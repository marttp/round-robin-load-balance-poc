package dev.tpcoder.routing.loadbalance;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lb")
public class LoadBalancerController {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerController.class);

    private final TrafficDistributable roundRobinLoadBalancer;

    public LoadBalancerController(
            @Qualifier("roundRobinLoadBalancer") TrafficDistributable roundRobinLoadBalancer
    ) {
        this.roundRobinLoadBalancer = roundRobinLoadBalancer;
    }

    @PostMapping("round-robin")
    public ResponseEntity<JsonNode> roundRobin(@RequestBody JsonNode body) {
        logger.debug("Received request to round robin load balancer");
        var result = roundRobinLoadBalancer.distributeTraffic(body);
        return ResponseEntity.ok(result);
    }
}
