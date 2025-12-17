package com.wallet.walletservice.Exceptions;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long userId, BigDecimal current, BigDecimal required) {
        super("Insufficient balance for userId=" + userId + " current=" + current + " required=" + required);
    }
}