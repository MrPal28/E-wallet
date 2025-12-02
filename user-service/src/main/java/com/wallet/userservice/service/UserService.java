package com.wallet.userservice.service;

import com.wallet.userservice.dto.*;

public interface UserService {

    // ---------------------- AUTH -------------------------
    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser();


    // ---------------------- USER PROFILE -----------------
    UserResponse updateUserProfile(Long userId, UpdateUserRequest request);

    UserResponse getUserById(Long userId);

    UserResponse getUserByEmail(String email);


    // ---------------------- KYC MANAGEMENT ----------------
    void submitKyc(Long userId, KycRequest request);

    KycStatusResponse getKycStatus(Long userId);
}
