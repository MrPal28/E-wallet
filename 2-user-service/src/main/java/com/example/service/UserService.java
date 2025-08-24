package com.example.service;
import com.example.dto.KycRequest;
import com.example.dto.KycStatusResponse;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserResponse;
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