package com.wallet.userservice.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.wallet.userservice.dto.RegisterRequest;
import com.wallet.userservice.dto.UserResponse;
import com.wallet.userservice.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Mapper {
  
  private final PasswordEncoder passwordEncoder;

  
  public User mapToEntity(RegisterRequest request){
		return User.builder().name(request.getUserName())
							 .email(request.getEmail())
							 .password(passwordEncoder.encode(request.getPassword()))
							 .phoneNo(request.getPhoneNo())
							 .address(request.getAddress())
							 .kycVerified(false)
							 .build();
	}
	
  
	public UserResponse mapToResponse(User user){
		return UserResponse.builder().id(user.getUserId()).userName(user.getName())
											 .email(user.getEmail())
											 .address(user.getAddress())
											 .phoneNo(user.getPhoneNo())
											 .kycVerified(user.isKycVerified())
											 .role("USER").build();
	}
}
