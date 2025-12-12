package com.wallet.walletservice.service;

import com.wallet.walletservice.constants.LedgerType;
import com.wallet.walletservice.constants.WalletStatus;
import com.wallet.walletservice.dto.WalletResponse;
import com.wallet.walletservice.entity.WalletAccount;
import com.wallet.walletservice.entity.WalletLedger;
import com.wallet.walletservice.repo.WalletAccountRepository;
import com.wallet.walletservice.repo.WalletLedgerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Production-ready Wallet service
 *
 * Principles:
 *  - Wallet state is the source of truth in DB.
 *  - Ledger provides idempotency and audit trail.
 *  - Use DB row lock + Redis distributed lock for cross-instance safety.
 *  - Use Redis cache for reads (eventual consistency), update cache after writes.
 *  - Publish optional events (non-blocking).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletAccountRepository walletRepo;
    private final WalletLedgerRepository ledgerRepo;
    private final RedisTemplate<String, String> redisTemplate;         // configured in RedisConfig
    private final Optional<KafkaTemplate<String, String>> kafkaTemplate; // optional, injected if available

    private static final String BAL_CACHE_PREFIX = "wallet:balance:";
    private static final Duration BAL_CACHE_TTL = Duration.ofMinutes(10);
    private static final String LOCK_PREFIX = "wallet:lock:";         // lock key format
    private static final long LOCK_EXPIRY_SECONDS = 10L;             // lock expiry to avoid deadlocks

    /* ===========================
       Register Wallet On User Creation
       =========================== */
    @Override
    public WalletResponse registerNewWallet(Long userId) {
        log.info("Registering wallet for userId={}", userId);
        WalletAccount wallet = WalletAccount.builder()
                .userId(userId)
                .status(WalletStatus.ACTIVE)
                .currentBalance(BigDecimal.ZERO)
                .build();
        walletRepo.save(wallet);

        // warm cache
        setBalanceCache(userId, BigDecimal.ZERO);
        return toResponse(wallet);
    }

    /* ===========================
       Read - try cache first
       =========================== */
    @Override
    public WalletResponse getWalletByUserId(Long userId) {
        String cacheKey = BAL_CACHE_PREFIX + userId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache hit for wallet={} value={}", userId, cached);
                return WalletResponse.builder()
                        .userId(userId)
                        .currentBalance(new BigDecimal(cached))
                        .build();
            }
        } catch (Exception e) {
            log.warn("Redis read failed for key={}, falling back to DB. cause={}", cacheKey, e.toString());
        }

        WalletAccount wallet = walletRepo.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        // update cache best-effort
        setBalanceCache(wallet.getUserId(), wallet.getCurrentBalance());
        return toResponse(wallet);
    }

    @Override
    public String getWalletStatus(Long userId) {
        return walletRepo.findByUserId(userId)
                .map(w -> w.getStatus().name())
                .orElse("NOT_FOUND");
    }

    /* ===========================
       CREDIT - idempotent + atomic
       =========================== */
    @Transactional
    @Override
    public WalletResponse credit(Long userId, BigDecimal amount, String referenceId, String referenceType) {
        if (amount == null || amount.signum() <= 0) throw new InvalidAmountException(amount);

        final String lockKey = LOCK_PREFIX + userId;
        final String lockValue = UUID.randomUUID().toString();

        // 1) idempotency check BEFORE attempting locks (fast path)
        if (ledgerRepo.existsByReferenceId(referenceId)) {
            log.info("Duplicate credit request ignored referenceId={}", referenceId);
            WalletAccount existingWallet = walletRepo.findByUserId(userId).orElseThrow(() -> new WalletNotFoundException(userId));
            return toResponse(existingWallet);
        }

        boolean locked = tryAcquireLock(lockKey, lockValue, LOCK_EXPIRY_SECONDS);
        if (!locked) {
            // conservative: avoid immediate failure; caller/transaction-service should retry or handle pending
            log.warn("Could not acquire redis lock for userId={}, referenceId={}", userId, referenceId);
            throw new ConcurrentModificationException("Resource busy, try again");
        }

        try {
            // 2) row-level lock in DB for strong consistency
            WalletAccount wallet = walletRepo.findByUserIdForUpdate(userId)
                    .orElseThrow(() -> new WalletNotFoundException(userId));

            // 3) second idempotency check under lock to avoid race
            if (ledgerRepo.existsByReferenceId(referenceId)) {
                log.info("Duplicate credit (post-lock) ignored referenceId={}", referenceId);
                return toResponse(wallet);
            }

            // 4) append ledger (audit + idempotency)
            WalletLedger ledger = WalletLedger.builder()
                    .walletId(wallet.getWalletId())
                    .amount(amount)
                    .type(LedgerType.CREDIT)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .timestamp(Instant.now())
                    .remarks("CREDIT via event/api")
                    .build();
            ledgerRepo.save(ledger);

            // 5) apply balance change
            BigDecimal newBalance = wallet.getCurrentBalance().add(amount);
            wallet.setCurrentBalance(newBalance);
            walletRepo.save(wallet);

            // 6) cache update & publish event (best-effort)
            setBalanceCache(userId, newBalance);
            publishBalanceEvent(userId, newBalance, referenceId, "CREDIT");

            return toResponse(wallet);

        } finally {
            safeReleaseLock(lockKey, lockValue);
        }
    }

    /* ===========================
       DEBIT - idempotent + atomic + validation
       =========================== */
    @Transactional
    @Override
    public WalletResponse debit(Long userId, BigDecimal amount, String referenceId, String referenceType) {
        if (amount == null || amount.signum() <= 0) throw new InvalidAmountException(amount);

        final String lockKey = LOCK_PREFIX + userId;
        final String lockValue = UUID.randomUUID().toString();

        // idempotency fast-path
        if (ledgerRepo.existsByReferenceId(referenceId)) {
            log.info("Duplicate debit request ignored referenceId={}", referenceId);
            WalletAccount existingWallet = walletRepo.findByUserId(userId).orElseThrow(() -> new WalletNotFoundException(userId));
            return toResponse(existingWallet);
        }

        boolean locked = tryAcquireLock(lockKey, lockValue, LOCK_EXPIRY_SECONDS);
        if (!locked) {
            log.warn("Could not acquire redis lock for userId={}, referenceId={}", userId, referenceId);
            throw new ConcurrentModificationException("Resource busy, try again");
        }

        try {
            WalletAccount wallet = walletRepo.findByUserIdForUpdate(userId)
                    .orElseThrow(() -> new WalletNotFoundException(userId));

            // idempotency under lock
            if (ledgerRepo.existsByReferenceId(referenceId)) {
                log.info("Duplicate debit (post-lock) ignored referenceId={}", referenceId);
                return toResponse(wallet);
            }

            // validation
            if (wallet.getCurrentBalance().compareTo(amount) < 0) {
                log.warn("Insufficient balance for userId={} balance={} requested={}", userId, wallet.getCurrentBalance(), amount);
                throw new InsufficientBalanceException(userId, wallet.getCurrentBalance(), amount);
            }

            // ledger entry
            WalletLedger ledger = WalletLedger.builder()
                    .walletId(wallet.getWalletId())
                    .amount(amount.negate()) // store negative amount for debit
                    .type(LedgerType.DEBIT)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .timestamp(Instant.now())
                    .remarks("DEBIT via event/api")
                    .build();
            ledgerRepo.save(ledger);

            // apply
            BigDecimal newBalance = wallet.getCurrentBalance().subtract(amount);
            wallet.setCurrentBalance(newBalance);
            walletRepo.save(wallet);

            setBalanceCache(userId, newBalance);
            publishBalanceEvent(userId, newBalance, referenceId, "DEBIT");

            return toResponse(wallet);

        } finally {
            safeReleaseLock(lockKey, lockValue);
        }
    }

    /* ===========================
       Helpers
       =========================== */

    private WalletResponse toResponse(WalletAccount wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUserId())
                .currentBalance(wallet.getCurrentBalance())
                .status(wallet.getStatus())
                .build();
    }

    private void setBalanceCache(Long userId, BigDecimal balance) {
        String key = BAL_CACHE_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, balance.toPlainString(), BAL_CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to set balance cache key={} balance={} cause={}", key, balance, e.toString());
        }
    }

    private void publishBalanceEvent(Long userId, BigDecimal newBalance, String referenceId, String action) {
        try {
            if (kafkaTemplate.isPresent()) {
                // Build a small JSON string or use a serializer in production
                String payload = String.format("{\"userId\":%d,\"balance\":\"%s\",\"referenceId\":\"%s\",\"action\":\"%s\"}",
                        userId, newBalance.toPlainString(), referenceId, action);
                kafkaTemplate.get().send("wallet-balance-events", userId.toString(), payload);
                log.debug("Published balance event for userId={} action={} newBalance={}", userId, action, newBalance);
            }
        } catch (Exception e) {
            // non-fatal: publishing failures shouldn't break wallet update
            log.warn("Failed to publish balance event for userId={} cause={}", userId, e.toString());
        }
    }

    /**
     * Simple Redis SETNX lock with expiry.
     * Returns true when lock acquired.
     */
    private boolean tryAcquireLock(String key, String value, long expireSeconds) {
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Redis lock acquisition failed for key={} cause={}", key, e.toString());
            return false;
        }
    }

    /**
     * Safe release: delete only when value matches (simple compare-and-del).
     * Note: In production use a Lua script to make delete atomic, or use Redisson.
     */
    private void safeReleaseLock(String key, String value) {
        try {
            // get-and-compare-delete pattern (not atomic). For stronger guarantees use EVAL Lua script.
            String current = redisTemplate.opsForValue().get(key);
            if (value.equals(current)) {
                redisTemplate.delete(key);
                log.debug("Released lock key={}", key);
            } else {
                log.debug("Not releasing lock key={}, holder mismatch", key);
            }
        } catch (Exception e) {
            log.warn("Failed to release lock key={} cause={}", key, e.toString());
        }
    }

    /* ===========================
       Domain Exceptions (small, move to separate files if needed)
       =========================== */
    public static class WalletNotFoundException extends RuntimeException {
        public WalletNotFoundException(Long userId) {
            super("Wallet not found for userId=" + userId);
        }
    }

    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(Long userId, BigDecimal current, BigDecimal required) {
            super("Insufficient balance for userId=" + userId + " current=" + current + " required=" + required);
        }
    }

    public static class InvalidAmountException extends RuntimeException {
        public InvalidAmountException(BigDecimal amount) {
            super("Invalid amount: " + amount);
        }
    }

    public static class ConcurrentModificationException extends RuntimeException {
        public ConcurrentModificationException(String msg) { super(msg); }
    }
}
