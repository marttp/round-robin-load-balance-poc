package dev.tpcoder.routing.loadbalance;

import com.fasterxml.jackson.databind.JsonNode;

public interface TrafficDistributable {

    JsonNode distributeTraffic(JsonNode body);
}
