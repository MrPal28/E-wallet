package com.wallet.walletservice.Exceptions;

 /* Domain Exceptions (small, move to separate files if needed) */
    public class WalletNotFoundException extends RuntimeException {
        public WalletNotFoundException(Long userId) {
            super("Wallet not found for userId=" + userId);
        }
    }

