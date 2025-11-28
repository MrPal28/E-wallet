package com.wallet.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${spring.cloud.gateway.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${spring.cloud.gateway.cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${spring.cloud.gateway.cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${spring.cloud.gateway.cors.allow-credentials}")
    private boolean allowCredentials;

    @Bean
    public CorsWebFilter corsWebFilter(GlobalCorsProperties globalCorsProperties) {

        CorsConfiguration corsConfig = new CorsConfiguration();

        // Dynamic, environment-driven CORS rules
        corsConfig.setAllowedOrigins(allowedOrigins);
        corsConfig.setAllowedMethods(allowedMethods);
        corsConfig.setAllowedHeaders(allowedHeaders);
        corsConfig.setAllowCredentials(allowCredentials);

        // Prevent empty origin list in production
        if (CollectionUtils.isEmpty(allowedOrigins)) {
            throw new IllegalStateException(
                    "CORS configuration failed: no origins defined. Set 'gateway.cors.allowed-origins' in environment."
            );
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
