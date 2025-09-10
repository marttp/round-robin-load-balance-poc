package dev.tpcoder.routing.loadbalance.roundrobin;

import com.fasterxml.jackson.databind.JsonNode;
import dev.tpcoder.routing.loadbalance.TrafficDistributable;
import org.springframework.stereotype.Service;

@Service
public class RoundRobinLoadBalancer implements TrafficDistributable {

    @Override
    public JsonNode distributeTraffic(JsonNode body) {
        return null;
    }
}
