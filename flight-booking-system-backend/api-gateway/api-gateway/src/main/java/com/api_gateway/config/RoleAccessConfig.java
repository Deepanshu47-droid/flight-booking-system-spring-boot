package com.api_gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "role-access")
@Getter
@Setter
public class RoleAccessConfig {
    private Map<String, List<String>> unauthorized = new HashMap<>();
}
