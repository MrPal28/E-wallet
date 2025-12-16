package com.wallet.transactionservice.service.Implementation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.wallet.transactionservice.Entity.Transaction;
import com.wallet.transactionservice.constants.TransactionStatus;
import com.wallet.transactionservice.constants.TransactionType;
import com.wallet.transactionservice.eventController.WalletEventPublisher;
import com.wallet.transactionservice.eventDto.WalletDebitRequest;
import com.wallet.transactionservice.repository.TransactionRepository;
import com.wallet.transactionservice.service.TransactionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repo;
    private final WalletEventPublisher walletPublisher;

    @Override
    public Transaction transfer(Long fromUser, Long toUser, BigDecimal amount) {

        String ref = UUID.randomUUID().toString();

        Transaction tx = Transaction.builder()
                .fromUserId(fromUser)
                .toUserId(toUser)
                .amount(amount)
                .type(TransactionType.WALLET_TO_WALLET)
                .status(TransactionStatus.INITIATED)
                .referenceId(ref)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        repo.save(tx);

        // Debit sender
        walletPublisher.debit(
                WalletDebitRequest.builder()
                        .userId(fromUser)
                        .amount(amount)
                        .referenceId(ref)
                        .referenceType("TRANSACTION")
                        .requestSource("TRANSACTION-SERVICE")
                        .eventTimestamp(Instant.now().toEpochMilli())
                        .build()
        );

        tx.setStatus(TransactionStatus.PROCESSING);
        repo.save(tx);

        return tx;
    }
}
