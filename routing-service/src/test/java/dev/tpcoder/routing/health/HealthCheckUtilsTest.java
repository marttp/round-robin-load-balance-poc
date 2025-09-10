package dev.tpcoder.routing.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckUtilsTest {

    @Test
    void shouldConvertEchoEndpointToHealthEndpoint() {
        // Given
        String echoUrl = "http://localhost:8081/api/v1/echo";
        
        // When
        String healthUrl = HealthCheckUtils.convertToHealthEndpoint(echoUrl);
        
        // Then
        assertEquals("http://localhost:8081/actuator/health", healthUrl);
    }

    @Test
    void shouldConvertDifferentPathToHealthEndpoint() {
        // Given
        String serviceUrl = "http://localhost:8082/some/other/path";
        
        // When
        String healthUrl = HealthCheckUtils.convertToHealthEndpoint(serviceUrl);
        
        // Then
        assertEquals("http://localhost:8082/actuator/health", healthUrl);
    }

    @Test
    void shouldHandleUrlWithoutPath() {
        // Given
        String baseUrl = "http://localhost:8083";
        
        // When
        String healthUrl = HealthCheckUtils.convertToHealthEndpoint(baseUrl);
        
        // Then
        assertEquals("http://localhost:8083/actuator/health", healthUrl);
    }

    @Test
    void shouldHandleHttpsUrls() {
        // Given
        String httpsUrl = "https://api.example.com:8443/api/v1/echo";
        
        // When
        String healthUrl = HealthCheckUtils.convertToHealthEndpoint(httpsUrl);
        
        // Then
        assertEquals("https://api.example.com:8443/actuator/health", healthUrl);
    }

    @Test
    void shouldHandleUrlWithMultiplePaths() {
        // Given
        String complexUrl = "http://localhost:9090/service/api/v2/endpoint";
        
        // When
        String healthUrl = HealthCheckUtils.convertToHealthEndpoint(complexUrl);
        
        // Then
        assertEquals("http://localhost:9090/actuator/health", healthUrl);
    }

    @Test
    void shouldReturnTrueForHealthyResponse() {
        // Given
        String healthyResponse = "{\"status\":\"UP\"}";
        
        // When
        boolean isHealthy = HealthCheckUtils.isHealthyResponse(healthyResponse);
        
        // Then
        assertTrue(isHealthy);
    }

    @Test
    void shouldReturnFalseForUnhealthyResponse() {
        // Given
        String unhealthyResponse = "{\"status\":\"DOWN\"}";
        
        // When
        boolean isHealthy = HealthCheckUtils.isHealthyResponse(unhealthyResponse);
        
        // Then
        assertFalse(isHealthy);
    }

    @Test
    void shouldReturnFalseForNullResponse() {
        // Given
        String nullResponse = null;
        
        // When
        boolean isHealthy = HealthCheckUtils.isHealthyResponse(nullResponse);
        
        // Then
        assertFalse(isHealthy);
    }

    @Test
    void shouldReturnFalseForEmptyResponse() {
        // Given
        String emptyResponse = "";
        
        // When
        boolean isHealthy = HealthCheckUtils.isHealthyResponse(emptyResponse);
        
        // Then
        assertFalse(isHealthy);
    }

    @Test
    void shouldReturnFalseForMalformedResponse() {
        // Given
        String malformedResponse = "Not a JSON response - This one is pure text";
        
        // When
        boolean isHealthy = HealthCheckUtils.isHealthyResponse(malformedResponse);
        
        // Then
        assertFalse(isHealthy);
    }

    @Test
    void shouldHandlePartialHealthyResponse() {
        // Given - Response with UP status with other content
        String partialResponse = "{\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}";
        
        // When
        boolean isHealthy = HealthCheckUtils.isHealthyResponse(partialResponse);
        
        // Then
        assertTrue(isHealthy);
    }
}