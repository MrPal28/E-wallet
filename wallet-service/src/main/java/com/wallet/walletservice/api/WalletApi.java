package com.wallet.walletservice.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.walletservice.dto.WalletResponse;
import com.wallet.walletservice.service.WalletService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;




@RestController
@RequestMapping("/wallet")
@Slf4j
public class WalletApi {

  @Autowired
  private WalletService walletService;
  // @Autowired
  // private WalletAccountRepository walletAccountRepository;

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

  @GetMapping("/status")
  public ResponseEntity<String> getStatus(HttpServletRequest request) {
      String userIdHeader = request.getHeader("x-user-id");

      if (userIdHeader == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                  .body(null); // Or throw custom UnauthorizedException
      }

      Long userId = Long.parseLong(userIdHeader);

      String status = walletService.getWalletStatus(userId);
      return ResponseEntity.ok(status);
  }

  // @PostMapping("/add")
  // public ResponseEntity<String> addMoney(@RequestHeader("x-user-id") String userIdHeader){
  //   WalletAccount wallet = walletAccountRepository.findByUserId(Long.parseLong(userIdHeader)).orElseThrow(()-> new RuntimeException("Wallet not found"));
  //   wallet.setCurrentBalance(wallet.getCurrentBalance().add(new BigDecimal(5000)));
  //   walletAccountRepository.save(wallet);
  //   log.info("Money added successfully");
  //   walletService.setWalletCache(wallet);
  //   log.info("Wallet cached successfully");
  //   return ResponseEntity.ok("Money added successfully");
  // }

}
