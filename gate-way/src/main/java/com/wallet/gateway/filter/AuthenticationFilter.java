package com.wallet.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.wallet.gateway.util.JwtUtil;

import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    @Value("${spring.cloud.gateway.security.header.username:x-username}")
    private String usernameHeader;

    @Value("${spring.cloud.gateway.security.header.user-id:x-user-id}")
    private String userIdHeader;

    @Value("${spring.cloud.gateway.security.header.role:x-role}")
    private String roleHeader;

    @SuppressWarnings("null")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || authHeader.isBlank()) {
                log.warn("Missing Authorization header");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }

            if (!authHeader.startsWith("Bearer ")) {
                log.warn("Authorization header format invalid");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Authorization header format");
            }

            String token = authHeader.substring(7);

            if (token.isBlank()) {
                log.warn("Bearer token empty");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
            }

            try {
                if (!jwtUtil.validateToken(token)) {
                    log.warn("Token invalid or expired");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
                }

                String username = jwtUtil.extractUsername(token);
                Integer userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // Mutate request to pass claims downstream
                request = request.mutate()
                        .header(usernameHeader, username)
                        .header(userIdHeader, String.valueOf(userId))
                        .header(roleHeader, role)
                        .build();

            } catch (Exception ex) {
                log.error("Unauthorized access: {}", ex.getMessage());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
            }
        }

        return chain.filter(exchange.mutate().request(request).build());
    }
}
