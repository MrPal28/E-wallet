package com.example.demo.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.demo.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter {

    
    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("**** Gateway filtering started ****");

        ServerHttpRequest request = exchange.getRequest();

        // Check if route is secured
        if (validator.isSecured.test(request)) {

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Missing Authorization header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                throw new RuntimeException("Invalid Authorization header format");
            }

            try {
                if (!jwtUtil.validateToken(token)) {
                    throw new RuntimeException("Invalid or expired token");
                }

                // Extract custom claims from token
                String username = jwtUtil.extractUsername(token);
                Integer userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                // Mutate request to include custom headers
                request = exchange.getRequest()
                        .mutate()
                        .header("x-username", username)
                        .header("x-user-id", String.valueOf(userId))
                        .header("x-role", role)
                        .build();

            } catch (Exception e) {
                System.out.println("Unauthorized access: " + e.getMessage());
                throw new RuntimeException("Unauthorized access to application");
            }
        }

        // Continue filter chain with (possibly) modified request
        return chain.filter(exchange.mutate().request(request).build());
    }
}
