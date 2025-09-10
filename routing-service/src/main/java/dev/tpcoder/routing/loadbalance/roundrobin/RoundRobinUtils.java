package dev.tpcoder.routing.loadbalance.roundrobin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinUtils {

    public static String getNextRoundRobinHost(CopyOnWriteArrayList<String> hostsList, AtomicInteger counter) {
        if (hostsList.isEmpty()) {
            throw new IllegalArgumentException("Hosts list cannot be empty");
        }
        int totalSize = hostsList.size();
        int currentIndex = counter.getAndUpdate(c -> (c + 1) % totalSize);
        return hostsList.get(currentIndex);
    }

    public static boolean isAllHostsUnhealthy(ConcurrentHashMap<String, Boolean> hostHealthStatusMap) {
        if (hostHealthStatusMap.isEmpty()) {
            return true;
        }
        return hostHealthStatusMap.values().stream().noneMatch(Boolean.TRUE::equals);
    }

    public static boolean isHostHealthy(String hostUrl, ConcurrentHashMap<String, Boolean> hostHealthStatusMap) {
        return Boolean.TRUE.equals(hostHealthStatusMap.get(hostUrl));
    }
}