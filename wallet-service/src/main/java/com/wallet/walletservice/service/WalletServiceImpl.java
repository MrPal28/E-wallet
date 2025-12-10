package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;


import com.example.demo.dto.WalletResponse;
import com.example.demo.entity.Wallet;
import com.example.demo.entity.WalletStatus;
import com.example.demo.repo.WalletRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    // helper method to map Wallet -> WalletResponse
    private WalletResponse toResponse(Wallet wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUserId())
                .walletBalance(wallet.getWalletBalance())
                .status(wallet.getStatus())
                .lastUpdated(wallet.getLastUpdated())
                .build();
    }

    @Override
    public Wallet registerNewWallet(Long userId) {
        Wallet wallet = new Wallet();
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setUserId(userId);
        wallet.setWalletBalance(new BigDecimal("50.00"));
        wallet.setLastUpdated(LocalDate.now());
        walletRepository.save(wallet);
        return wallet;
    }

    @Override
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return toResponse(wallet);
    }

    @Override
    public String getWalletStatus(Long userId) {
       Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(()-> new RuntimeException("Wallet not found"));
       return wallet.getStatus().name();
    }

    // @Override
    // @Transactional
    // public WalletResponse addMoney(Long userId, AddMoneyRequest request) {
    //     Wallet wallet = walletRepository.findByUserId(userId)
    //             .orElseThrow(() -> new RuntimeException("Wallet not found"));

    //     wallet.setWalletBalance(wallet.getWalletBalance().add(request.getAmount()));
    //     wallet.setLastUpdated(LocalDate.now());
    //     walletRepository.save(wallet);

    //     return toResponse(wallet);
    // }

    // @Override
    // @Transactional
    // public WalletResponse transferMoney(Long fromUserId, TransferRequest request) {
    //     Wallet fromWallet = walletRepository.findByUserId(fromUserId)
    //             .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

    //     Wallet toWallet = walletRepository.findByUserId(request.getToUserId())
    //             .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

    //     if (fromWallet.getWalletBalance().compareTo(request.getAmount()) < 0) {
    //         throw new RuntimeException("Insufficient balance");
    //     }

    //     fromWallet.setWalletBalance(fromWallet.getWalletBalance().subtract(request.getAmount()));
    //     toWallet.setWalletBalance(toWallet.getWalletBalance().add(request.getAmount()));

    //     fromWallet.setLastUpdated(LocalDate.now());
    //     toWallet.setLastUpdated(LocalDate.now());

    //     walletRepository.save(fromWallet);
    //     walletRepository.save(toWallet);

    //     return toResponse(fromWallet);
    // }

// @Override
// @Transactional
// public WalletResponse transferMoney(Long fromUserId, TransferRequest request) {
//     // Step 1: Fetch wallets
//     Wallet fromWallet = walletRepository.findByUserId(fromUserId)
//             .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

//     Wallet toWallet = walletRepository.findByUserId(request.getToUserId())
//             .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

//     // Step 2: Prepare transaction events
//     TransactionEvent debitEvent = new TransactionEvent();
//     debitEvent.setTransferId(System.currentTimeMillis()); // simple unique ID
//     debitEvent.setFromUserId(fromUserId);
//     debitEvent.setToUserId(request.getToUserId());
//     debitEvent.setAmount(request.getAmount());
//     debitEvent.setType("DEBIT");
//     debitEvent.setStatus("PENDING");
//     // debitEvent.setNote(request.getNote());

//     TransactionEvent creditEvent = new TransactionEvent();
//     creditEvent.setTransferId(debitEvent.getTransferId());
//     creditEvent.setFromUserId(fromUserId);
//     creditEvent.setToUserId(request.getToUserId());
//     creditEvent.setAmount(request.getAmount());
//     creditEvent.setType("CREDIT");
//     creditEvent.setStatus("PENDING");
//     // creditEvent.setNote(request.getNote());

//     // Step 3: Validate sender balance
//     if (fromWallet.getWalletBalance().compareTo(request.getAmount()) < 0) {
//         debitEvent.setStatus("FAILED");
//         creditEvent.setStatus("FAILED");
//         statusProducer.sendTransactionStatus(debitEvent);
//         statusProducer.sendTransactionStatus(creditEvent);
//         throw new RuntimeException("Insufficient balance");
//     }

//     // Step 4: Debit sender and credit receiver
//     fromWallet.setWalletBalance(fromWallet.getWalletBalance().subtract(request.getAmount()));
//     toWallet.setWalletBalance(toWallet.getWalletBalance().add(request.getAmount()));
//     fromWallet.setLastUpdated(LocalDate.now());
//     toWallet.setLastUpdated(LocalDate.now());
//     walletRepository.save(fromWallet);
//     walletRepository.save(toWallet);

//     // Step 5: Update events to SUCCESS and publish
//     debitEvent.setStatus("SUCCESS");
//     creditEvent.setStatus("SUCCESS");
//     statusProducer.sendTransactionStatus(debitEvent);
//     statusProducer.sendTransactionStatus(creditEvent);

//     // Step 6: Return updated sender wallet info
//     return WalletResponse.builder()
//             .walletId(fromWallet.getWalletId())
//             .userId(fromWallet.getUserId())
//             .walletBalance(fromWallet.getWalletBalance())
//             .status(fromWallet.getStatus())
//             .lastUpdated(fromWallet.getLastUpdated())
//             .build();
// }



}
