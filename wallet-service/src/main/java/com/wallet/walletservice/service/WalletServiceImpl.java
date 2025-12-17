package com.wallet.walletservice.service;

import com.wallet.walletservice.EventHandler.WalletCreditResultPublisher;
import com.wallet.walletservice.EventHandler.WalletDebitResultPublisher;
import com.wallet.walletservice.Exceptions.ConcurrentModificationException;
import com.wallet.walletservice.Exceptions.InsufficientBalanceException;
import com.wallet.walletservice.Exceptions.InvalidAmountException;
import com.wallet.walletservice.Exceptions.WalletNotFoundException;
import com.wallet.walletservice.constants.LedgerType;
import com.wallet.walletservice.constants.TransactionStatus;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Production Wallet service
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
    private final WalletCreditResultPublisher creditResultPublisher;
    private final WalletDebitResultPublisher debitResultPublisher;

    private static final String BAL_CACHE_PREFIX = "wallet:balance:";
    private static final Duration BAL_CACHE_TTL = Duration.ofMinutes(10);
    private static final String LOCK_PREFIX = "wallet:lock:";         // lock key format
    private static final long LOCK_EXPIRY_SECONDS = 10L;             // lock expiry to avoid deadlocks

    /* Register Wallet On User Creation */
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

    /* 
       Read - try cache first
     */
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

    /* CREDIT - idempotent + atomic */
    @Transactional
    @Override
    public WalletResponse credit(
            Long userId,
            BigDecimal amount,
            String referenceId,
            String referenceType) {
        if (amount == null || amount.signum() <= 0) {
            creditResultPublisher.publish(
                    userId,
                    amount,
                    null,
                    referenceId,
                    TransactionStatus.FAILED,
                    "INVALID_AMOUNT");
            throw new InvalidAmountException(amount);
        }

        final String lockKey = LOCK_PREFIX + userId;
        final String lockValue = UUID.randomUUID().toString();

        // Idempotency fast-path
        Optional<WalletLedger> existingLedger = ledgerRepo.findByReferenceId(referenceId);

        if (existingLedger.isPresent()) {
            WalletAccount wallet = walletRepo.findByUserId(userId)
                    .orElseThrow(() -> new WalletNotFoundException(userId));

            // IMPORTANT: still publish SUCCESS again
            creditResultPublisher.publish(
                    userId,
                    amount,
                    wallet.getCurrentBalance(),
                    referenceId,
                    TransactionStatus.SUCCESS,
                    null);

            return toResponse(wallet);
        }

        boolean locked = tryAcquireLock(lockKey, lockValue, LOCK_EXPIRY_SECONDS);
        if (!locked) {
            creditResultPublisher.publish(
                    userId,
                    amount,
                    null,
                    referenceId,
                    TransactionStatus.FAILED,
                    "WALLET_LOCK_BUSY");
            throw new ConcurrentModificationException("Wallet busy");
        }

        try {
            //  DB row lock
            WalletAccount wallet = walletRepo.findByUserIdForUpdate(userId)
                    .orElseThrow(() -> new WalletNotFoundException(userId));

            //  Idempotency re-check under lock
            if (ledgerRepo.existsByReferenceId(referenceId)) {
                creditResultPublisher.publish(
                        userId,
                        amount,
                        wallet.getCurrentBalance(),
                        referenceId,
                        TransactionStatus.SUCCESS,
                        null);
                return toResponse(wallet);
            }

            //  Ledger append
            ledgerRepo.save(
                    WalletLedger.builder()
                            .walletId(wallet.getWalletId())
                            .amount(amount)
                            .type(LedgerType.CREDIT)
                            .referenceId(referenceId)
                            .referenceType(referenceType)
                            .timestamp(Instant.now())
                            .remarks("CREDIT")
                            .build());

            //  Apply balance
            BigDecimal newBalance = wallet.getCurrentBalance().add(amount);
            wallet.setCurrentBalance(newBalance);
            walletRepo.save(wallet);

            //  Cache update
            setBalanceCache(userId, newBalance);

            // Publish SUCCESS
            creditResultPublisher.publish(
                    userId,
                    amount,
                    newBalance,
                    referenceId,
                    TransactionStatus.SUCCESS,
                    null);

            return toResponse(wallet);

        } catch (Exception ex) {
            log.error("Credit failed ref={} user={}", referenceId, userId, ex);

            creditResultPublisher.publish(
                    userId,
                    amount,
                    null,
                    referenceId,
                    TransactionStatus.FAILED,
                    ex.getMessage());

            throw ex;

        } finally {
            safeReleaseLock(lockKey, lockValue);
        }
    }


    @Transactional
    @Override
    public WalletResponse debit(
            Long userId,
            BigDecimal amount,
            String referenceId,
            String referenceType) {
        if (amount == null || amount.signum() <= 0) {
            debitResultPublisher.publish(
                    userId,
                    amount,
                    null,
                    referenceId,
                    TransactionStatus.FAILED,
                    "INVALID_AMOUNT");
            throw new InvalidAmountException(amount);
        }

        final String lockKey = LOCK_PREFIX + userId;
        final String lockValue = UUID.randomUUID().toString();

        // Idempotency fast-path
        Optional<WalletLedger> existingLedger = ledgerRepo.findByReferenceId(referenceId);

        if (existingLedger.isPresent()) {
            WalletAccount wallet = walletRepo.findByUserId(userId)
                    .orElseThrow(() -> new WalletNotFoundException(userId));

            // Re-emit SUCCESS for idempotent replay
            debitResultPublisher.publish(
                    userId,
                    amount,
                    wallet.getCurrentBalance(),
                    referenceId,
                    TransactionStatus.SUCCESS,
                    null);

            return toResponse(wallet);
        }

        boolean locked = tryAcquireLock(lockKey, lockValue, LOCK_EXPIRY_SECONDS);
        if (!locked) {
            debitResultPublisher.publish(
                    userId,
                    amount,
                    null,
                    referenceId,
                    TransactionStatus.FAILED,
                    "WALLET_LOCK_BUSY");
            throw new ConcurrentModificationException("Wallet busy");
        }

        try {
            // DB row lock
            WalletAccount wallet = walletRepo.findByUserIdForUpdate(userId)
                    .orElseThrow(() -> new WalletNotFoundException(userId));

            // Idempotency re-check under lock
            if (ledgerRepo.existsByReferenceId(referenceId)) {
                debitResultPublisher.publish(
                        userId,
                        amount,
                        wallet.getCurrentBalance(),
                        referenceId,
                        TransactionStatus.SUCCESS,
                        null);
                return toResponse(wallet);
            }

            // Balance validation
            if (wallet.getCurrentBalance().compareTo(amount) < 0) {
                debitResultPublisher.publish(
                        userId,
                        amount,
                        wallet.getCurrentBalance(),
                        referenceId,
                        TransactionStatus.FAILED,
                        "INSUFFICIENT_BALANCE");
                throw new InsufficientBalanceException(
                        userId,
                        wallet.getCurrentBalance(),
                        amount);
            }

            // Ledger entry
            ledgerRepo.save(
                    WalletLedger.builder()
                            .walletId(wallet.getWalletId())
                            .amount(amount.negate())
                            .type(LedgerType.DEBIT)
                            .referenceId(referenceId)
                            .referenceType(referenceType)
                            .timestamp(Instant.now())
                            .remarks("DEBIT")
                            .build());

            // Apply balance
            BigDecimal newBalance = wallet.getCurrentBalance().subtract(amount);
            wallet.setCurrentBalance(newBalance);
            walletRepo.save(wallet);

            setBalanceCache(userId, newBalance);

            // Publish SUCCESS
            debitResultPublisher.publish(
                    userId,
                    amount,
                    newBalance,
                    referenceId,
                    TransactionStatus.SUCCESS,
                    null);

            return toResponse(wallet);

        } catch (Exception ex) {
            log.error("Debit failed ref={} user={}", referenceId, userId, ex);

            // defensive: ensure FAILED is emitted if not already
            debitResultPublisher.publish(
                    userId,
                    amount,
                    null,
                    referenceId,
                    TransactionStatus.FAILED,
                    ex.getMessage());

            throw ex;

        } finally {
            safeReleaseLock(lockKey, lockValue);
        }
    }


    /* Helpers */
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

}
