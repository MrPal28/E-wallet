package com.wallet.transactionservice.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.transactionservice.dto.TransactionResponse;
import com.wallet.transactionservice.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/transfer")
    public TransactionResponse transfer(
            @RequestHeader("x-user-id") Long fromUser,
            @RequestParam Long toUser,
            @RequestParam BigDecimal amount) {

        return service.transfer(fromUser, toUser, amount);
    }
}
