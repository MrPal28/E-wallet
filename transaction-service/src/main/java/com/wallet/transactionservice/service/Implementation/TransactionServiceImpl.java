package com.wallet.transactionservice.service.Implementation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.wallet.transactionservice.Entity.Transaction;
import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.constants.TransactionType;
import com.wallet.transactionservice.dto.TransactionResponse;
import com.wallet.transactionservice.eventController.WalletEventPublisher;
import com.wallet.transactionservice.eventDto.WalletDebitRequest;
import com.wallet.transactionservice.repository.TransactionRepository;
import com.wallet.transactionservice.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repo;
    private final WalletEventPublisher walletPublisher;

    @Override
    public TransactionResponse transfer(Long fromUser, Long toUser, BigDecimal amount) {

        validate(fromUser, toUser, amount);

        String referenceId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // 1️⃣ Persist INITIATED transaction
        Transaction tx = Transaction.builder()
                .fromUserId(fromUser)
                .toUserId(toUser)
                .amount(amount)
                .type(TransactionType.WALLET_TO_WALLET)
                .status(TransactionStatus.INITIATED) // ✅ STAYS INITIATED
                .referenceId(referenceId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        repo.save(tx);

        try {
            // 2️⃣ Publish debit request (async)
            walletPublisher.debit(
                    WalletDebitRequest.builder()
                            .userId(fromUser)
                            .amount(amount)
                            .referenceId(referenceId)
                            .referenceType("TRANSACTION")
                            .requestSource("TRANSACTION-SERVICE")
                            .eventTimestamp(now.toEpochMilli())
                            .build()
            );

            // ❌ DO NOT change status here

        } catch (Exception ex) {
            // 3️⃣ Only publish failure if event publish itself fails
            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason("DEBIT_EVENT_PUBLISH_FAILED");
            tx.setUpdatedAt(Instant.now());
            repo.save(tx);
            throw ex;
        }

        // 4️⃣ Return ACK, not final result
        return TransactionResponse.from(tx);
    }

    private void validate(Long fromUser, Long toUser, BigDecimal amount) {
        if (fromUser.equals(toUser)) {
            throw new IllegalArgumentException("Sender and receiver cannot be same");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }
    }
}
