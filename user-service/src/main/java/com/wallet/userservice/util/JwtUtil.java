
package com.wallet.userservice.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtUtil {

    @Value("${spring.cloud.gateway.jwt.secret}")
    private String secretKey;

    @Value("${spring.cloud.gateway.jwt.allowed-skew:5000}") // 5 seconds by default
    private long allowedClockSkew;

    private SecretKey getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception ex) {
            log.error("Failed to decode JWT secret key: {}", ex.getMessage());
            throw new IllegalStateException("Invalid JWT Secret Key");
        }
    }

    private Claims getAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .setAllowedClockSkewSeconds(allowedClockSkew / 1000)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            log.warn("JWT parsing failed: {}", ex.getMessage());
            throw ex;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            boolean expired = isTokenExpired(token);
            if (expired) {
                log.warn("JWT token expired");
                return false;
            }
            // Parsing itself validates structure & signature
            getAllClaims(token);
            return true;
        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
