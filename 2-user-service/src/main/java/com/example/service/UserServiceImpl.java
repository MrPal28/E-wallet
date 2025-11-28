package com.example.service;

import java.time.LocalDateTime;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.dto.KycRequest;
import com.example.dto.KycStatusResponse;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterRequest;
import com.example.dto.UpdateUserRequest;
import com.example.dto.UserResponse;
import com.example.entity.Kyc;
import com.example.entity.User;
import com.example.exception.ApplicationException;
import com.example.exception.KycNotFoundException;
import com.example.exception.UserNotFoundException;
import com.example.repository.KycRepository;
import com.example.repository.UserRepository;
import com.example.util.AuthenticationFacadeImpl;
import com.example.util.KafkaProducer;
import com.example.util.Mapper;


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
