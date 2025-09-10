package dev.tpcoder.routing.loadbalance.roundrobin;

import com.fasterxml.jackson.databind.JsonNode;
import dev.tpcoder.routing.loadbalance.TrafficDistributable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoundRobinLoadBalancer implements TrafficDistributable {

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    private final RoundRobinConfiguration roundRobinConfiguration;

    public RoundRobinLoadBalancer(RoundRobinConfiguration roundRobinConfiguration) {
        this.roundRobinConfiguration = roundRobinConfiguration;
    }

    @Override
    public JsonNode distributeTraffic(JsonNode body) {
        var totalSize = roundRobinConfiguration.hostsList().size();
        var currentIndex = roundRobinConfiguration
                .roundRobinCounter()
                .getAndUpdate(c -> (c + 1) % totalSize);
        logger.debug("Current host is {}", roundRobinConfiguration.hostsList().get(currentIndex));
        return body;
    }
}
