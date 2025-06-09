package com.api_gateway.filter;

import com.api_gateway.config.RoleAccessConfig;
import com.api_gateway.exceptions.CustomException;
import com.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    private final RouteValidator validator;

    private final JwtUtil jwtUtil;
    private final RoleAccessConfig roleAccessConfig;


    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil, RoleAccessConfig roleAccessConfig) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
        this.roleAccessConfig = roleAccessConfig;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            log.info("Incoming request path: {}", path);

            if (validator.isSecured.test(exchange.getRequest())) {
                log.info("Request is secured. Checking for Authorization header...");

                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Authorization header is missing");
                    throw new CustomException("Missing Authorization header", 401);
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                log.debug("Raw Authorization header: {}", authHeader);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                    log.debug("Extracted token: {}", authHeader);
                } else {
                    log.warn("Authorization header does not start with Bearer");
                    throw new CustomException("Invalid Authorization header format", 401);
                }

                try {
                    jwtUtil.validateToken(authHeader);
                    log.info("Token validated successfully");

                    String username = jwtUtil.extractUsername(authHeader);
                    log.info("Username extracted: {}", username);

                    exchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-Username", username)
                                    .build())
                            .build();
                } catch (Exception e) {
                    log.error("Invalid token: {}", e.getMessage());
                    throw new CustomException("Unauthorized: Invalid or Expired Token", 401);
                }
                String userRole = jwtUtil.extractUserRole(authHeader);
                log.info("Role is {}", userRole);
                List<String> unauthorizedPaths = roleAccessConfig.getUnauthorized().getOrDefault(userRole, List.of());
                log.info("unauthorized paths for {} are {}",userRole, unauthorizedPaths.toString());
                boolean isNotAllowed = unauthorizedPaths.stream().anyMatch(path::startsWith);
                if (isNotAllowed) {
                    throw new CustomException("Forbidden: You don't have access to this resource", 403);
                }
            } else {
                log.info("Public endpoint detected. Skipping authentication");
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
