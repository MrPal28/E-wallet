package com.wallet.transactionservice.service.implementation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import com.wallet.transactionservice.dto.AddMoneyRequest;
import com.wallet.transactionservice.dto.TransactionResponse;
import com.wallet.transactionservice.dto.TransferRequest;
import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.repo.TransactionRepository;
import com.wallet.transactionservice.service.TransactionService;
import com.wallet.transactionservice.util.TransferMoneyEventProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransferMoneyEventProducer transferMoneyEventProducer;


    @Override
    public TransactionResponse getTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return mapToResponse(transaction);
    }


    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .fromUserId(transaction.getFromUserId())
                .toUserId(transaction.getToUserId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

//     @Override
// public List<TransactionResponse> getTransactionsByUserId(Long userId) {
//     List<Transaction> transactions = transactionRepository
//             .findByFromUserIdOrToUserId(userId, userId);

//     if (transactions.isEmpty()) {
//         throw new RuntimeException("No transactions found for user ID: " + userId);
//     }

//     return transactions.stream()
//             .map(this::mapToResponse)
//             .toList();
// }
    @Override
public List<TransactionResponse> getTransactionsByUserId(Long userId) {
    // User as sender → only DEBIT
    List<Transaction> debitTransactions = transactionRepository.findByFromUserIdAndType(userId, "DEBIT");

    // User as receiver → only CREDIT
    List<Transaction> creditTransactions = transactionRepository.findByToUserIdAndType(userId, "CREDIT");

    List<Transaction> allUserTransactions = new ArrayList<>();
    allUserTransactions.addAll(debitTransactions);
    allUserTransactions.addAll(creditTransactions);

    if (allUserTransactions.isEmpty()) {
        throw new RuntimeException("No transactions found for user ID: " + userId);
    }

    return allUserTransactions.stream()
            .map(this::mapToResponse)
            .toList();
}


    @Override
    public TransactionResponse topUp(Long userId, AddMoneyRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transferMoney'");
    }

    @Override
    public TransactionResponse transferMoney(Long fromUserId, TransferRequest request) {
        // Step 1: Extract required details from request
        Long toUserId = request.getToUserId();
        BigDecimal amount = request.getAmount();

        // Step 2: Call Producer to create transaction + publish event
        Transaction tx = transferMoneyEventProducer.initiateTransfer(fromUserId, toUserId, amount);

        // Step 3: Prepare and return response (currently transaction is PENDING)
        return TransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .fromUserId(tx.getFromUserId())
                .toUserId(tx.getToUserId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus().name())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
