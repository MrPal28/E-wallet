package com.example.demo.service.implementation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.TransactionEvent;
import com.example.demo.dto.TransactionRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.entity.Transaction;
import com.example.demo.repo.TransactionRepository;
import com.example.demo.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

   

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
                .status(transaction.getStatus())
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

}
