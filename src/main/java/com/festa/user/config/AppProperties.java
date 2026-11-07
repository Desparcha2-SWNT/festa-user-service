package com.festa.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "festa.user")
public record AppProperties(
        String piiKey,
        AuthAdmin authAdmin,
        Outbox outbox,
        Rabbit rabbit
) {
    public record AuthAdmin(String baseUrl, String roleEndpoint) {}

    public record Outbox(Duration pollInterval, int batchSize) {}

    public record Rabbit(
            String exchange,
            String inboxQueue,
            String retryQueue,
            String dlqQueue
    ) {}
}