package com.example.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.KycRequest;
import com.example.dto.KycStatusResponse;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserResponse;
import com.example.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth/users")        
public class UserApi {

	@Autowired
	private UserService userService;
	
 @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    // 2. Login User
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }


    // 4. Get Current Authenticated User (from token)
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    // 5. Update User Profile
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(@PathVariable Long userId,
                                                          @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    // 6. Get User by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    // 7. Get User by Email
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    // 8. Submit KYC
    @PostMapping("/{userId}/kyc")
    public ResponseEntity<Void> submitKyc(@PathVariable Long userId,
                                          @Valid @RequestBody KycRequest request) {
        userService.submitKyc(userId, request);
        return ResponseEntity.ok().build();
    }

    // 9. Get KYC Status
    @GetMapping("/{userId}/kyc/status")
    public ResponseEntity<KycStatusResponse> getKycStatus(@PathVariable Long userId) {
        KycStatusResponse response = userService.getKycStatus(userId);
        return ResponseEntity.ok(response);
    }


}
