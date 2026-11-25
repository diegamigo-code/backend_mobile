package com.wim100.backend.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        Seed seed,
        Candidatas candidatas
) {

    public record Jwt(String secret, Duration expiration, String issuer) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Seed(boolean enabled, String defaultPassword) {
    }

    public record Candidatas(List<String> dominiosPermitidos) {
    }
}
