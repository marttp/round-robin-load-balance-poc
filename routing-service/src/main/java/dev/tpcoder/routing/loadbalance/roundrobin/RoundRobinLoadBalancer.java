package dev.tpcoder.routing.loadbalance.roundrobin;

import com.fasterxml.jackson.databind.JsonNode;
import dev.tpcoder.routing.exception.NoHostAvailableException;
import dev.tpcoder.routing.exception.UpstreamException;
import dev.tpcoder.routing.loadbalance.TrafficDistributable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

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
        var timeoutMs = roundRobinConfiguration.getRoutingProperties().getTimeout().toMillis();
        this.restClient = restClientBuilder
                .requestFactory(createClientHttpRequestFactory(timeoutMs))
                .build();
    }

    @Override
    public JsonNode distributeTraffic(JsonNode body) {
        var hostsList = roundRobinConfiguration.getHostsList();
        var healthStatusMap = roundRobinConfiguration.getHostHealthStatusMap();
        if (hostsList.isEmpty() || RoundRobinUtils.isAllHostsUnhealthy(healthStatusMap)) {
            throw new NoHostAvailableException("No healthy hosts available");
        }

        var routingProperties = roundRobinConfiguration.getRoutingProperties();
        var failureThreshold = routingProperties.getFailureAttemptThreshold();

        // Retry using failure attempt threshold
        while (failureThreshold > 0) {
            String selectedHost = RoundRobinUtils.getNextRoundRobinHost(hostsList, roundRobinConfiguration.getRoundRobinCounter());
            if (RoundRobinUtils.isHostHealthy(selectedHost, healthStatusMap)) {
                try {
                    return upstreamRequest(body, selectedHost);
                } catch (Exception e) {
                    logger.error("Failed to route request to host: {}", selectedHost, e);
                    failureThreshold--;
                    logger.debug("Retry remaining: {}", failureThreshold);
                }
            }
        }
        throw new UpstreamException("Failed to route request to hosts");
    }

    private JsonNode upstreamRequest(JsonNode body, String host) {
        return restClient.post()
                .uri(host)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private HttpComponentsClientHttpRequestFactory createClientHttpRequestFactory(long timeoutMs) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        clientHttpRequestFactory.setConnectionRequestTimeout(Duration.ofMillis(timeoutMs));
        return clientHttpRequestFactory;
    }

}
