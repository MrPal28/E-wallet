package com.wallet.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    // -------- ENVIRONMENT CONFIGURATION ----------

    @Value("${USER_SERVICE_JWT_SECRET}")
    private String secretKey;

    @Value("${USER_SERVICE_JWT_EXPIRATION:900000}") // default 15 minutes
    private long jwtExpirationMs;

    @Value("${USER_SERVICE_JWT_ALLOWED_SKEW:5}") // default 5 seconds
    private long allowedClockSkewSeconds;

    // ---------------------------------------------------------

    private SecretKey getSigningKey() {
        try {
            byte[] decoded = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(decoded);
        } catch (Exception ex) {
            log.error("Invalid JWT secret key: {}", ex.getMessage());
            throw new IllegalStateException("JWT signing key is invalid");
        }
    }

    private Claims parseAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .clockSkewSeconds(allowedClockSkewSeconds)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            log.warn("JWT parsing failed: {}", ex.getMessage());
            throw ex;
        }
    }

    // ---------------------------------------------------------
    // CLAIM EXTRACTION
    // ---------------------------------------------------------

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
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

    // ---------------------------------------------------------
    // TOKEN VALIDATION
    // ---------------------------------------------------------

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);

            if (username == null || !username.equals(userDetails.getUsername())) {
                log.warn("JWT username mismatch");
                return false;
            }

            if (isTokenExpired(token)) {
                log.warn("JWT token expired");
                return false;
            }

            parseAllClaims(token); // validates integrity + signature
            return true;

        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------
    // TOKEN GENERATION
    // ---------------------------------------------------------

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Extract userId + role from our CustomUserDetails
        claims.put("userId", ((com.wallet.userservice.config.CustomUserDetails) userDetails).getId());
        claims.put("role", ((com.wallet.userservice.config.CustomUserDetails) userDetails).getRole());

        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject) {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
}
