package com.wallet.walletservice.service;


import java.math.BigDecimal;

import com.wallet.walletservice.dto.WalletResponse;
import com.wallet.walletservice.entity.WalletAccount;

public interface WalletService {
    WalletResponse registerNewWallet(Long userId);
    WalletResponse getWalletByUserId(Long userId);
    WalletResponse credit(Long userId, BigDecimal amount, String referenceId, String referenceType);
    WalletResponse debit(Long userId, BigDecimal amount, String referenceId, String referenceType);
    String getWalletStatus(Long userId);
}

