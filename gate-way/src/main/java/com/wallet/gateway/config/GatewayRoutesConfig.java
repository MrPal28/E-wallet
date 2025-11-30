package com.wallet.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

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

    private final KeyResolver userIdResolver;
    private final RedisRateLimiter walletRateLimiter;
    private final RedisRateLimiter transactionRateLimiter;

    public GatewayRoutesConfig(KeyResolver userIdResolver, @Qualifier("walletRateLimiter")RedisRateLimiter walletRateLimiter, @Qualifier("transactionRateLimiter")RedisRateLimiter transactionRateLimiter) {
        this.userIdResolver = userIdResolver;
        this.walletRateLimiter = walletRateLimiter;
        this.transactionRateLimiter = transactionRateLimiter;
    }

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
                    .filters(f -> f.requestRateLimiter(c -> {
                        c.setKeyResolver(userIdResolver);
                        c.setRateLimiter(walletRateLimiter);
                        c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    }))
                     .uri("lb://" + walletServiceId))

            // Transaction Service
            .route("transaction-service-route", r ->
                    r.path(transactionPath)
                    .filters(f -> f.requestRateLimiter(c -> {
                        c.setKeyResolver(userIdResolver);
                        c.setRateLimiter(transactionRateLimiter);
                        c.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    }))
                     .uri("lb://" + transactionServiceId))

            .build();
    }
}
