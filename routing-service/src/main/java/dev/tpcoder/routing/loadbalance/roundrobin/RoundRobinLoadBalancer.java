package dev.tpcoder.routing.loadbalance.roundrobin;

import com.fasterxml.jackson.databind.JsonNode;
import dev.tpcoder.routing.loadbalance.TrafficDistributable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

        if (hostsList.isEmpty()) {
            throw new RuntimeException("No healthy hosts available");
        }

        var totalSize = hostsList.size();
        var currentIndex = roundRobinConfiguration
                .getRoundRobinCounter()
                .getAndUpdate(c -> (c + 1) % totalSize);

        String selectedHost = hostsList.get(currentIndex);
        logger.info("Routing request to host: {} (index: {})", selectedHost, currentIndex);

        try {
            return restClient.post()
                    .uri(selectedHost)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            logger.error("Failed to route request to host: {}", selectedHost, e);
            throw new RuntimeException("Failed to route request to host: " + selectedHost, e);
        }
    }
}
