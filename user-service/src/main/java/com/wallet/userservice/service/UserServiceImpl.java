package com.wallet.userservice.service;

import com.wallet.userservice.dto.*;
import com.wallet.userservice.entity.Kyc;
import com.wallet.userservice.entity.User;
import com.wallet.userservice.exception.ApplicationException;
import com.wallet.userservice.exception.KycNotFoundException;
import com.wallet.userservice.exception.UserNotFoundException;
import com.wallet.userservice.repository.KycRepository;
import com.wallet.userservice.repository.UserRepository;
import com.wallet.userservice.util.AuthenticationFacade;
import com.wallet.userservice.util.JwtUtil;
import com.wallet.userservice.util.KafkaProducer;
import com.wallet.userservice.util.Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final KycRepository kycRepo;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationFacade authenticationFacade;
    private final UserDetailsService userDetailsService;
    private final KafkaProducer kafkaProducer;
    private final Mapper mapper;

    // -----------------------------------------------------------
    // REGISTER
    // -----------------------------------------------------------
    @Transactional
    @Override
    public UserResponse register(RegisterRequest request) {

        log.info("Register request received for email={}", request.getEmail());

        // Email must be unique
        userRepo.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new ApplicationException("User with this email already exists");
        });

        // Map → save
        User user = mapper.toEntity(request);
        user = userRepo.save(user);

        UserResponse userResponse = mapper.toResponse(user);

        // Fire Kafka event asynchronously
        kafkaProducer.sendUserEvent(user.getEmail(), userResponse);

        log.info("User registered successfully: userId={}", user.getUserId());

        return userResponse;
    }

    // -----------------------------------------------------------
    // LOGIN
    // -----------------------------------------------------------
    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for email={}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        final var userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        final String jwtToken = jwtUtil.generateToken(userDetails);

        return LoginResponse.builder()
                .email(request.getEmail())
                .role("USER")  // future: dynamic roles
                .token(jwtToken)
                .build();
    }

    // -----------------------------------------------------------
    // CURRENT USER
    // -----------------------------------------------------------
    @Override
    public UserResponse getCurrentUser() {
        String email = authenticationFacade.getAuthentication().getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapper.toResponse(user);
    }

    // -----------------------------------------------------------
    // UPDATE PROFILE
    // -----------------------------------------------------------
    @Transactional
    @Override
    public UserResponse updateUserProfile(Long userId, UpdateUserRequest request) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAddress(request.getAddress());
        user.setPhoneNo(request.getPhoneNo());

        userRepo.save(user);

        return mapper.toResponse(user);
    }

    // -----------------------------------------------------------
    // GET USER BY ID
    // -----------------------------------------------------------
    @Override
    public UserResponse getUserById(Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapper.toResponse(user);
    }

    // -----------------------------------------------------------
    // GET USER BY EMAIL
    // -----------------------------------------------------------
    @Override
    public UserResponse getUserByEmail(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapper.toResponse(user);
    }

    // -----------------------------------------------------------
    // KYC SUBMISSION
    // -----------------------------------------------------------
    @Transactional
    @Override
    public void submitKyc(Long userId, KycRequest request) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isKycVerified()) {
            throw new ApplicationException("KYC already completed");
        }

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

        kycRepo.save(kyc);
        userRepo.save(user);

        log.info("KYC submitted for userId={}", user.getUserId());
    }

    // -----------------------------------------------------------
    // KYC STATUS
    // -----------------------------------------------------------
    @Override
    public KycStatusResponse getKycStatus(Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Kyc kyc = kycRepo.findByUserId(userId)
                .orElseThrow(() -> new KycNotFoundException("KYC not found"));

        return KycStatusResponse.builder()
                .kycVerified(user.isKycVerified())
                .status(kyc.getStatus())
                .build();
    }
}
