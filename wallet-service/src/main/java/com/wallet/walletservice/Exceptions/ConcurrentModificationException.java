package com.wallet.walletservice.Exceptions;

public class ConcurrentModificationException extends RuntimeException {
    public ConcurrentModificationException(String msg) { super(msg); }
}