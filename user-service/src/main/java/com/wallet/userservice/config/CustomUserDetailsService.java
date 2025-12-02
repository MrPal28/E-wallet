package com.wallet.userservice.config;

import com.wallet.userservice.entity.User;
import com.wallet.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service

@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.debug("Attempting login lookup for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("Invalid email or user does not exist");
                });

        if (!user.isActive()) {
            log.warn("Inactive user attempted login: {}", email);
            throw new UsernameNotFoundException("User account is inactive");
        }

        return new CustomUserDetails(user);
    }
}
