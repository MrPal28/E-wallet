package com.example.demo.service;

import com.example.demo.dto.AddMoneyRequest;
import com.example.demo.dto.TransactionEvent;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.WalletResponse;
import com.example.demo.entity.Wallet;

public interface WalletService {
    Wallet registerNewWallet(Long userId);
    WalletResponse getWalletByUserId(Long userId);
    WalletResponse addMoney(Long userId, AddMoneyRequest request);
    WalletResponse transferMoney(Long fromUserId, TransferRequest request);
}

