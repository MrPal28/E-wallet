package com.wallet.userservice.api;

import com.wallet.userservice.dto.*;
import com.wallet.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserApi {

    private final UserService userService;

    // ---------------------------------------------------
    // REGISTER
    // ---------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("API: Register request received for email={}", request.getEmail());
        return ResponseEntity.ok(userService.register(request));
    }

    // ---------------------------------------------------
    // LOGIN
    // ---------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("API: Login attempt for email={}", request.getEmail());
        return ResponseEntity.ok(userService.login(request));
    }

    // ---------------------------------------------------
    // CURRENT USER
    // ---------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // ---------------------------------------------------
    // UPDATE USER PROFILE
    // ---------------------------------------------------
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, request));
    }

    // ---------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // ---------------------------------------------------
    // GET USER BY EMAIL
    // ---------------------------------------------------
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // ---------------------------------------------------
    // SUBMIT KYC
    // ---------------------------------------------------
    @PostMapping("/{userId}/kyc")
    public ResponseEntity<Void> submitKyc(
            @PathVariable Long userId,
            @Valid @RequestBody KycRequest request
    ) {
        userService.submitKyc(userId, request);
        return ResponseEntity.ok().build();
    }

    // ---------------------------------------------------
    // KYC STATUS
    // ---------------------------------------------------
    @GetMapping("/{userId}/kyc/status")
    public ResponseEntity<KycStatusResponse> getKycStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getKycStatus(userId));
    }
}
