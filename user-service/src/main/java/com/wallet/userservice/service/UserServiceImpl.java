package com.wallet.userservice.service;

import java.time.LocalDateTime;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.wallet.userservice.dto.KycRequest;
import com.wallet.userservice.dto.KycStatusResponse;
import com.wallet.userservice.dto.LoginRequest;
import com.wallet.userservice.dto.LoginResponse;
import com.wallet.userservice.dto.RegisterRequest;
import com.wallet.userservice.dto.UpdateUserRequest;
import com.wallet.userservice.dto.UserResponse;
import com.wallet.userservice.entity.Kyc;
import com.wallet.userservice.entity.User;
import com.wallet.userservice.exception.ApplicationException;
import com.wallet.userservice.exception.KycNotFoundException;
import com.wallet.userservice.exception.UserNotFoundException;
import com.wallet.userservice.repository.KycRepository;
import com.wallet.userservice.repository.UserRepository;
import com.wallet.userservice.util.AuthenticationFacadeImpl;
import com.wallet.userservice.util.KafkaProducer;
import com.wallet.userservice.util.Mapper;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepo;
	private final KycRepository kycRepo;
	private final JwtService jwtService;
	private final  AuthenticationManager authenticationManager;
	private final AuthenticationFacadeImpl authenticationFacadeImpl;
	private final UserDetailsService userDetailsService;
	private final KafkaProducer kafkaProducer;
	private final Mapper mapper;
	
	
	@Override
	public UserResponse register(RegisterRequest request) {
		userRepo.findByEmail(request.getUserName()).ifPresent(u -> {
			throw new ApplicationException("User Already Present");
		});
		User user = mapper.mapToEntity(request);
		userRepo.save(user);
		UserResponse userResponse = mapper.mapToResponse(user);
		kafkaProducer.messageToKafka(user.getEmail(), userResponse);
		return userResponse;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail() , request.getPassword()));
    final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
    final String jwtToken = jwtService.generateToken(userDetails);
		return LoginResponse.builder()
		.email(request.getEmail())
		.token(jwtToken)
		.role("USER")
		.build();
	}

	@Override
	public UserResponse getCurrentUser() {
		String loggedInUserEmail = authenticationFacadeImpl.getAuthentication().getName();
		User user = userRepo.findByEmail(loggedInUserEmail)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
				return mapper.mapToResponse(user);
	}
	@Override
	public UserResponse updateUserProfile(Long userId, UpdateUserRequest request) {
		User user = userRepo.findById(userId).orElseThrow(()-> new UserNotFoundException("No User"));
		if(user!=null){
			user.setAddress(request.getAddress());
			user.setPhoneNo(request.getPhoneNo());
		}
		return mapper.mapToResponse(userRepo.save(user));
	}
	@Override
	public UserResponse getUserById(Long userId) {
	 User user = userRepo.findById(userId).orElseThrow(()-> new UserNotFoundException("No User"));
	 return mapper.mapToResponse(user);
	}
	@Override
	public UserResponse getUserByEmail(String email) {
		User user = userRepo.findByEmail(email)
							.orElseThrow(() -> new UserNotFoundException(email + " not Found"));
		return mapper.mapToResponse(user);
	}
	@Override
	public void submitKyc(Long userId, KycRequest request) {
		User user = userRepo.findById(userId).orElseThrow(()->new UserNotFoundException("No user found)"));
		if(!user.isKycVerified()){
			Kyc kyc = Kyc.builder()
					.userId(user.getUserId())
					.panNumber(request.getPanNumber())
					.aadharNumber(request.getAadhaarNumber())
					.address(request.getAddress())
					.kycVerified(true)
					.status("VERIFIED")
					.lastUpdatedAt(LocalDateTime.now())
					.build();
					user.setKycVerified(true);
					userRepo.save(user);
					kycRepo.save(kyc);
		}
	}
	@Override
	public KycStatusResponse getKycStatus(Long userId) {
		User user = userRepo.findById(userId).orElseThrow(()->new UserNotFoundException("No user found"));
		Kyc kyc = kycRepo.findByUserId(userId).orElseThrow(()->new KycNotFoundException("No KYC found"));
		return KycStatusResponse.builder()
				.kycVerified(user.isKycVerified())
				.status(kyc.getStatus())
				.build();
	}

}
