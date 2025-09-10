package dev.tpcoder.routing.loadbalance.roundrobin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tpcoder.routing.exception.NoHostAvailableException;
import dev.tpcoder.routing.exception.UpstreamException;
import dev.tpcoder.routing.config.RoutingProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundRobinLoadBalancerTest {

    private RoundRobinLoadBalancer loadBalancer;
    private ObjectMapper objectMapper;
    private MockWebServer mockWebServer;
    private RoundRobinConfiguration roundRobinConfiguration;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String mockUrl = mockWebServer.url("/api/v1/echo").toString();
        RoutingProperties routingProperties = new RoutingProperties(
                mockUrl,
                Duration.ofMillis(500),
                2,
                Duration.ofSeconds(10)
        );
        roundRobinConfiguration = new RoundRobinConfiguration(routingProperties);
        RestClient.Builder clientBuilder = RestClient.builder();

        loadBalancer = new RoundRobinLoadBalancer(roundRobinConfiguration, clientBuilder);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldAbleToGetSuccessResponse() throws JsonProcessingException {
        var requestBody = objectMapper.readTree("{\"transactionId\":\"TXN123\"}");
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"transactionId\":\"TXN123\"}")
                .addHeader("Content-Type", "application/json"));
        var result = loadBalancer.distributeTraffic(requestBody);
        assertEquals("TXN123", result.get("transactionId").asText());
    }

    @Test
    void shouldThrowExceptionWhenHostListEmpty() throws IOException {
        mockWebServer.shutdown();

        RoutingProperties emptyProps = new RoutingProperties(
                "",
                Duration.ofMillis(500),
                2,
                Duration.ofSeconds(10)
        );
        RoundRobinConfiguration emptyConfig = new RoundRobinConfiguration(emptyProps);
        var emptyLoadBalancer = new RoundRobinLoadBalancer(emptyConfig, RestClient.builder());

        var requestBody = objectMapper.readTree("{\"transactionId\":\"TXN123\"}");
        assertThrows(NoHostAvailableException.class, () -> emptyLoadBalancer.distributeTraffic(requestBody));
    }

    @Test
    void shouldThrowExceptionWhenAllHostsFail() throws JsonProcessingException {
        // All host fail setup
        var statusTable = roundRobinConfiguration.getHostHealthStatusMap();
        statusTable.replaceAll((_, _) -> false);
        var requestBody = objectMapper.readTree("{\"transactionId\":\"TXN123\"}");
        assertThrows(NoHostAvailableException.class, () -> loadBalancer.distributeTraffic(requestBody));
    }

    @Test
    void shouldRetryAndFailAfterAttemptThresholdWithHttpErrors() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        var requestBody = objectMapper.readTree("{\"transactionId\":\"TXN123\"}");
        assertThrows(UpstreamException.class, () -> loadBalancer.distributeTraffic(requestBody));
    }

    @Test
    void shouldRetryAndFailAfterAttemptThresholdWithTimeouts() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"transactionId\":\"TXN123\"}")
                .setBodyDelay(1000, TimeUnit.MILLISECONDS));
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"transactionId\":\"TXN123\"}")
                .setBodyDelay(1000, TimeUnit.MILLISECONDS));

        var requestBody = objectMapper.readTree("{\"transactionId\":\"TXN123\"}");
        assertThrows(UpstreamException.class, () -> loadBalancer.distributeTraffic(requestBody));
    }
}