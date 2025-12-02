package com.wallet.userservice.util;

import com.wallet.userservice.dto.RegisterRequest;
import com.wallet.userservice.dto.UserResponse;
import com.wallet.userservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Mapper {

    private final PasswordEncoder passwordEncoder;
		
    public Mapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toEntity(RegisterRequest request) {

        log.debug("Mapping RegisterRequest to User entity for email={}", request.getEmail());

        return User.builder()
                .name(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNo(request.getPhoneNo())
                .address(request.getAddress())
                .kycVerified(false)
                .active(true)  // PRODUCTION: ensure new users are active
                .role("USER")  // PRODUCTION: avoid hardcoding in multiple layers
                .build();
    }

    public UserResponse toResponse(User user) {

        log.debug("Mapping User entity to UserResponse for id={}", user.getUserId());

        return UserResponse.builder()
                .id(user.getUserId())
                .userName(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phoneNo(user.getPhoneNo())
                .kycVerified(user.isKycVerified())
                .role(user.getRole())
                .build();
    }
}
