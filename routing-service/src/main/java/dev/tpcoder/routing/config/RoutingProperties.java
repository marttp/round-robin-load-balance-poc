package dev.tpcoder.routing.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class RoutingProperties {

    @Value("${routing.upstream.hosts}")
    private String hosts;

    @Value("${routing.upstream.timeout}")
    private Duration timeout;

    @Value("${routing.upstream.total-timeout}")
    private Duration totalTimeout;

    @Value("${routing.upstream.failure-attempt-threshold}")
    private int failureAttemptThreshold;

    @Value("${routing.health-check.interval}")
    private Duration healthCheckInterval;

    private List<String> upstreamHosts;

    @PostConstruct
    public void init() {
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

    public Duration getTotalTimeout() {
        return totalTimeout;
    }

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }
}