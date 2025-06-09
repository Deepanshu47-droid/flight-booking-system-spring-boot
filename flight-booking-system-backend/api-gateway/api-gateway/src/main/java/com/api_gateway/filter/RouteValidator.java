package com.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/login/",
            "/api/auth/register/user",
            "/api/auth/verify-otp/user",
            "/api/auth/reset/verify-otp/",
            "/api/auth/reset-password/",
            "/api/flights/search"
    );

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        log.info("Checking if request path '{}' is secured", path);

        boolean isActuator = path.contains("/actuator/");
        boolean isOpenApi = openApiEndpoints.stream().anyMatch(path::startsWith);

        boolean isSecured = !(isOpenApi || isActuator);


        if (isSecured) {
            log.info("Path '{}' is considered SECURED", path);
        } else {
            log.info("Path '{}' is considered OPEN (public)", path);
        }

        return isSecured;
    };
}
