package dev.tpcoder.routing.loadbalance.roundrobin;

import com.fasterxml.jackson.databind.JsonNode;
import dev.tpcoder.routing.loadbalance.TrafficDistributable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RoundRobinLoadBalancer implements TrafficDistributable {

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    private final RoundRobinConfiguration roundRobinConfiguration;
    private final RestClient restClient;

    public RoundRobinLoadBalancer(
            RoundRobinConfiguration roundRobinConfiguration,
            RestClient.Builder restClientBuilder
    ) {
        this.roundRobinConfiguration = roundRobinConfiguration;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public JsonNode distributeTraffic(JsonNode body) {
        var hostsList = roundRobinConfiguration.getHostsList();
        var healthStatusMap = roundRobinConfiguration.getHostHealthStatusMap();
        if (hostsList.isEmpty() || isAllHostsUnhealthy(healthStatusMap)) {
            throw new RuntimeException("No healthy hosts available");
        }
        var threshold = roundRobinConfiguration.getRoutingProperties().getFailureAttemptThreshold();
        // Retry by using attempt
        while (threshold > 0) {
            String selectedHost = getNextRoundRobinHost(hostsList);
            if (healthStatusMap.get(selectedHost)) {
                try {
                    return upstreamRequest(body, selectedHost);
                } catch (Exception e) {
                    logger.error("Failed to route request to host: {}", selectedHost, e);
                    threshold--;
                    logger.debug("Retry remaining: {}", threshold);
                }
            }
        }
        throw new RuntimeException("Failed to route request to hosts");
    }

    private JsonNode upstreamRequest(JsonNode body, String host) {
        return restClient.post()
                .uri(host)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private String getNextRoundRobinHost(CopyOnWriteArrayList<String> hostsList) {
        var totalSize = hostsList.size();
        var currentIndex = roundRobinConfiguration
                .getRoundRobinCounter()
                .getAndUpdate(c -> (c + 1) % totalSize);
        return hostsList.get(currentIndex);
    }

    private boolean isAllHostsUnhealthy(ConcurrentHashMap<String, Boolean> hostHealthStatusMap) {
        return hostHealthStatusMap.values().stream().noneMatch(Boolean.TRUE::equals);
    }

}
