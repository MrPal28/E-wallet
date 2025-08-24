package com.example.demo.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AddMoneyRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.WalletResponse;
import com.example.demo.service.WalletService;

import jakarta.servlet.http.HttpServletRequest;




@RestController
@RequestMapping("/wallet")
public class WalletApi {

  @Autowired
  private WalletService walletService;

  @GetMapping("/balance")
  public ResponseEntity<WalletResponse> getBalance(HttpServletRequest request){
     String userIdHeader = request.getHeader("x-user-id");

    if (userIdHeader == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(null); // Or throw custom UnauthorizedException
    }

    Long userId = Long.parseLong(userIdHeader);

    WalletResponse wallet = walletService.getWalletByUserId(userId);
    return ResponseEntity.ok(wallet);
  }

  @PutMapping("/add")
  public ResponseEntity<WalletResponse> addMoney(@RequestBody AddMoneyRequest request, HttpServletRequest httpRequest) {
    Long userId = Long.parseLong(httpRequest.getHeader("x-user-id"));
    WalletResponse response = walletService.addMoney(userId,request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/transfer")
  public ResponseEntity<WalletResponse> transferMoney(@RequestBody TransferRequest request,
      HttpServletRequest httpRequest) {
    Long fromUserId = Long.parseLong(httpRequest.getHeader("x-user-id"));
    WalletResponse response = walletService.transferMoney(fromUserId, request);
    return ResponseEntity.ok(response);
  }
	
}
