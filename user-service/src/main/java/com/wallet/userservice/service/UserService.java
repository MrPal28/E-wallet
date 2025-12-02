package com.wallet.userservice.service;
import com.wallet.userservice.dto.KycRequest;
import com.wallet.userservice.dto.KycStatusResponse;
import com.wallet.userservice.dto.LoginRequest;
import com.wallet.userservice.dto.LoginResponse;
import com.wallet.userservice.dto.RegisterRequest;
import com.wallet.userservice.dto.UpdateUserRequest;
import com.wallet.userservice.dto.UserResponse;
public interface UserService {
UserResponse register(RegisterRequest request);
LoginResponse login(LoginRequest request);
UserResponse getCurrentUser();
UserResponse updateUserProfile(Long userId, UpdateUserRequest request);
UserResponse getUserById(Long userId);
UserResponse getUserByEmail(String email);
void submitKyc(Long userId, KycRequest request);
KycStatusResponse getKycStatus(Long userId);

}