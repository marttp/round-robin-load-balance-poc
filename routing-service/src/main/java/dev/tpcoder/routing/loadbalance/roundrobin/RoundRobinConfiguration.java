package dev.tpcoder.routing.loadbalance.roundrobin;

import dev.tpcoder.routing.config.RoutingProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RoundRobinConfiguration {

    private final AtomicInteger roundRobinCounter;
    private final ConcurrentHashMap<String, Boolean> hostHealthStatusMap;
    private final CopyOnWriteArrayList<String> hostsList;

    public RoundRobinConfiguration(RoutingProperties routingProperties) {
        this.roundRobinCounter = new AtomicInteger(0);
        this.hostsList = new CopyOnWriteArrayList<>();
        this.hostHealthStatusMap = new ConcurrentHashMap<>();
        // Initialize all hosts
        for (String host : routingProperties.getHostsList()) {
            hostHealthStatusMap.put(host, true);
            hostsList.add(host);
        }
    }

    public AtomicInteger getRoundRobinCounter() {
        return roundRobinCounter;
    }

    public ConcurrentHashMap<String, Boolean> getHostHealthStatusMap() {
        return hostHealthStatusMap;
    }

    public CopyOnWriteArrayList<String> getHostsList() {
        return hostsList;
    }

}
