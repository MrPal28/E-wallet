package com.example.demo.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TransactionResponse;
import com.example.demo.dto.TransferRequest;
import com.example.demo.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {
  private final TransactionService transactionService;


  @GetMapping
  public ResponseEntity<List<TransactionResponse>> getMyTransaction(HttpServletRequest request){
     String userIdHeader = request.getHeader("x-user-id");

    if (userIdHeader == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(null); // Or throw custom UnauthorizedException
    }

    Long userId = Long.parseLong(userIdHeader);
    List<TransactionResponse> transactions = transactionService.getTransactionsByUserId(userId);
    return ResponseEntity.ok(transactions);
  }

  @PostMapping("/transfer")
  public ResponseEntity<TransactionResponse> transferMoney(HttpServletRequest request, @RequestBody TransferRequest transferRequest) {
      String userIdHeader = request.getHeader("x-user-id");

      if (userIdHeader == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(null); // Or throw custom UnauthorizedException
      }

      Long fromUserId = Long.parseLong(userIdHeader);
      TransactionResponse response = transactionService.transferMoney(fromUserId, transferRequest);
      return ResponseEntity.ok(response);
  }
}
