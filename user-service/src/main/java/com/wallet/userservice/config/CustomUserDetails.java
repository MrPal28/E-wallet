package com.wallet.userservice.config;

import com.wallet.userservice.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Future-ready: user.getRole() later if roles become dynamic
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getId() {
        return user.getUserId();
    }

    public String getRole() {
        return "USER"; // Future: return user.getRole()
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Allow flags later from DB if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Ideal for “failed attempts” lockout expansions
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Add password rotation policy if required
    }

    @Override
    public boolean isEnabled() {
        return user.isActive(); // PRODUCTION CHANGE
    }
}
