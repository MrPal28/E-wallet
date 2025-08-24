package com.example.demo.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

            // User Service
            .route("user-service", r -> r.path("/auth/**")
                .uri("lb://user-service"))

            // Wallet Service
            .route("3-wallet-service", r -> r.path("/wallet/**")
                .uri("lb://3-wallet-service"))

            // Transaction Service (optional)
            .route("4-transaction-service", r -> r.path("/transaction/**")
                .uri("lb://4-transaction-service"))

            .build();
    }
}
