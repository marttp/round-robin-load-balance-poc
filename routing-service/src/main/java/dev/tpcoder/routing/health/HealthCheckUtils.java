package dev.tpcoder.routing.health;

public class HealthCheckUtils {

    /**
     * Converts a service endpoint URL to its corresponding health check endpoint.
     * 
     * @param hostUrl the service endpoint URL (e.g., http://localhost:8081/api/v1/echo)
     * @return the health check endpoint URL (e.g., http://localhost:8081/actuator/health)
     */
    public static String convertToHealthEndpoint(String hostUrl) {
        // For testing only
        if (hostUrl.contains("/api/v1/echo")) {
            return hostUrl.replace("/api/v1/echo", "/actuator/health");
        }
        
        // Fallback: extract base URL using port delimiter
        int colonIndex = hostUrl.lastIndexOf(':');
        if (colonIndex > 7) { // After "http://" or "https://"
            int slashAfterPort = hostUrl.indexOf('/', colonIndex);
            if (slashAfterPort > 0) {
                String baseUrl = hostUrl.substring(0, slashAfterPort);
                return baseUrl + "/actuator/health";
            }
        }
        
        return hostUrl + "/actuator/health";
    }

    /**
     * Parses the health check response to determine if the service is healthy.
     * 
     * @param response the response body from the health check endpoint
     * @return true if the service is healthy (status: UP), false otherwise
     */
    public static boolean isHealthyResponse(String response) {
        return response != null && response.contains("\"status\":\"UP\"");
    }
}