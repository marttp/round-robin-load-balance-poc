package dev.tpcoder.routing.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class RoutingProperties {

    private final Duration timeout;

    private final int failureAttemptThreshold;

    private final Duration healthCheckInterval;

    private final List<String> upstreamHosts;

    public RoutingProperties(
            @Value("${routing.upstream.hosts}") String hosts,
            @Value("${routing.upstream.timeout}") Duration timeout,
            @Value("${routing.upstream.failure-attempt-threshold}") int failureAttemptThreshold,
            @Value("${routing.health-check.interval}") Duration healthCheckInterval
    ) {
        this.timeout = timeout;
        this.failureAttemptThreshold = failureAttemptThreshold;
        this.healthCheckInterval = healthCheckInterval;
        this.upstreamHosts = Arrays.asList(hosts.trim().split(","));
    }

    public List<String> getHostsList() {
        return this.upstreamHosts;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getFailureAttemptThreshold() {
        return failureAttemptThreshold;
    }

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }
}