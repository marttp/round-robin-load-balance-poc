package dev.tpcoder.routing.loadbalance.roundrobin;

import dev.tpcoder.routing.config.RoutingProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RoundRobinConfiguration {

    private final RoutingProperties routingProperties;

    public RoundRobinConfiguration(RoutingProperties routingProperties) {
        this.routingProperties = routingProperties;
    }

    @Bean
    public ConcurrentHashMap<String, Boolean> hostHealthStatusMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public CopyOnWriteArrayList<String> hostsList() {
        return new CopyOnWriteArrayList<>();
    }

    @Bean
    public AtomicInteger roundRobinCounter() {
        return new AtomicInteger(0);
    }

    @PostConstruct
    public void initializeHealthStatus(
            ConcurrentHashMap<String, Boolean> hostHealthStatusMap,
            CopyOnWriteArrayList<String> hostsList
    ) {
        // Initialize all hosts
        for (String host : routingProperties.getHostsList()) {
            hostHealthStatusMap.put(host, true);
            hostsList.add(host);
        }
    }
}
