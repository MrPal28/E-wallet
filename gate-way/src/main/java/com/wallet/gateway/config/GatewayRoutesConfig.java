package com.wallet.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${spring.cloud.gateway.server.webflux.routes.user.path}")
    private String userPath;

    @Value("${spring.cloud.gateway.server.webflux.routes.user.service-id}")
    private String userServiceId;

    @Value("${spring.cloud.gateway.server.webflux.routes.wallet.path}")
    private String walletPath;

    @Value("${spring.cloud.gateway.server.webflux.routes.wallet.service-id}")
    private String walletServiceId;

    @Value("${spring.cloud.gateway.server.webflux.routes.transaction.path}")
    private String transactionPath;

    @Value("${spring.cloud.gateway.server.webflux.routes.transaction.service-id}")
    private String transactionServiceId;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {

        return builder.routes()

            // User Service
            .route("user-service-route", r -> 
                    r.path(userPath)
                     .uri("lb://" + userServiceId))

            // Wallet Service
            .route("wallet-service-route", r -> 
                    r.path(walletPath)
                     .uri("lb://" + walletServiceId))

            // Transaction Service
            .route("transaction-service-route", r ->
                    r.path(transactionPath)
                     .uri("lb://" + transactionServiceId))

            .build();
    }
}
