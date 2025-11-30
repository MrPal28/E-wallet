package com.wallet.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import reactor.core.publisher.Mono;

@Configuration
public class RatelimiterConfig {
   @Bean
    public KeyResolver userIdResolver() {
        return exchange -> {
        String userId = exchange.getRequest()
                .getHeaders()
                .getFirst("x-user-id");
        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }

        return Mono.just(userId);
    };
  }

    // Wallet service: 20 req/sec, burst 40
    @Bean
    @Primary
    public RedisRateLimiter walletRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }

    // Transaction service: 10 req/sec, burst 20
    @Bean
    public RedisRateLimiter transactionRateLimiter() {
        return new RedisRateLimiter(10, 20);
    }
}
