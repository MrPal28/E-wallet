package com.wallet.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
public class RouteValidator {

        @SuppressWarnings("unused")
        private final List<String> openEndpoints;

        public final Predicate<ServerHttpRequest> isSecured;

        public RouteValidator(
                        @Value("${spring.cloud.gateway.security.open-endpoints}") List<String> openEndpoints) {
                this.openEndpoints = openEndpoints;
                this.isSecured = request -> openEndpoints
                                .stream()
                                .noneMatch(uri -> request.getURI().getPath().startsWith(uri));
                log.info("Bootstrapped {} open endpoints for gateway security", openEndpoints.size());
        }
}
