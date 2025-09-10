package dev.tpcoder.routing.loadbalance.roundrobin;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinUtilsTest {

    @Test
    void shouldReturnNextHostInRoundRobinFashion() {
        CopyOnWriteArrayList<String> hosts = new CopyOnWriteArrayList<>(List.of("host1", "host2", "host3"));
        AtomicInteger counter = new AtomicInteger(0);
        assertEquals("host1", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
        assertEquals("host2", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
        assertEquals("host3", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
        assertEquals("host1", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
    }

    @Test
    void shouldHandleSingleHostCorrectly() {
        CopyOnWriteArrayList<String> hosts = new CopyOnWriteArrayList<>();
        hosts.add("onlyhost");
        AtomicInteger counter = new AtomicInteger(0);

        assertEquals("onlyhost", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
        assertEquals("onlyhost", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
        assertEquals("onlyhost", RoundRobinUtils.getNextRoundRobinHost(hosts, counter));
    }

    @Test
    void shouldThrowExceptionForEmptyHostsList() {
        CopyOnWriteArrayList<String> emptyHosts = new CopyOnWriteArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        assertThrows(IllegalArgumentException.class, () -> {
            RoundRobinUtils.getNextRoundRobinHost(emptyHosts, counter);
        });
    }

    @Test
    void shouldReturnTrueWhenAllHostsUnhealthy() {
        ConcurrentHashMap<String, Boolean> healthMap = new ConcurrentHashMap<>();
        healthMap.put("host1", false);
        healthMap.put("host2", false);
        healthMap.put("host3", false);
        boolean result = RoundRobinUtils.isAllHostsUnhealthy(healthMap);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenSomeHostsHealthy() {
        ConcurrentHashMap<String, Boolean> healthMap = new ConcurrentHashMap<>();
        healthMap.put("host1", false);
        healthMap.put("host2", true);  // At least one healthy
        healthMap.put("host3", false);
        boolean result = RoundRobinUtils.isAllHostsUnhealthy(healthMap);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForEmptyHealthMap() {
        ConcurrentHashMap<String, Boolean> emptyHealthMap = new ConcurrentHashMap<>();
        boolean result = RoundRobinUtils.isAllHostsUnhealthy(emptyHealthMap);
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenHostIsHealthy() {
        ConcurrentHashMap<String, Boolean> healthMap = new ConcurrentHashMap<>();
        healthMap.put("host1", true);
        healthMap.put("host2", false);
        assertTrue(RoundRobinUtils.isHostHealthy("host1", healthMap));
        assertFalse(RoundRobinUtils.isHostHealthy("host2", healthMap));
    }

    @Test
    void shouldReturnFalseForUnknownHost() {
        ConcurrentHashMap<String, Boolean> healthMap = new ConcurrentHashMap<>();
        healthMap.put("host1", true);

        boolean result = RoundRobinUtils.isHostHealthy("unknownhost", healthMap);

        assertFalse(result);
    }
}