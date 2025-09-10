package dev.tpcoder.routing.health;

import dev.tpcoder.routing.config.RoutingProperties;
import dev.tpcoder.routing.loadbalance.roundrobin.RoundRobinConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class HealthCheckScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final RoutingProperties routingProperties;
    private final RoundRobinConfiguration roundRobinConfiguration;
    private final RestClient healthCheckClient;

    public HealthCheckScheduler(
            RoutingProperties routingProperties,
            RoundRobinConfiguration roundRobinConfiguration,
            RestClient.Builder restClientBuilder
    ) {
        this.routingProperties = routingProperties;
        this.roundRobinConfiguration = roundRobinConfiguration;
        this.healthCheckClient = restClientBuilder
                .build();
    }

    @Scheduled(
            initialDelayString = "#{routingProperties.healthCheckInterval.toMillis()}",
            fixedDelayString = "#{routingProperties.healthCheckInterval.toMillis()}"
    )
    public void performHealthChecks() {
        logger.debug("Starting health check cycle");
        ConcurrentHashMap<String, Boolean> healthStatusMap = roundRobinConfiguration.getHostHealthStatusMap();
        for (String hostUrl : routingProperties.getHostsList()) {
            checkHostHealth(hostUrl, healthStatusMap);
        }
        logger.debug("Health check cycle completed: {}", healthStatusMap);
    }

    private void checkHostHealth(String hostUrl, ConcurrentHashMap<String, Boolean> healthStatusMap) {
        try {
            String healthEndpoint = HealthCheckUtils.convertToHealthEndpoint(hostUrl);
            String response = healthCheckClient.get()
                    .uri(healthEndpoint)
                    .retrieve()
                    .body(String.class);
            boolean isHealthy = HealthCheckUtils.isHealthyResponse(response);
            healthStatusMap.put(hostUrl, isHealthy);
            logger.debug("Host {} health status: {}", hostUrl, isHealthy ? "HEALTHY" : "UNHEALTHY");
        } catch (Exception e) {
            logger.warn("Health check failed for host {}: {}", hostUrl, e.getMessage());
            healthStatusMap.put(hostUrl, false);
        }
    }
}