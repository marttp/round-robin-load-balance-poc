package dev.tpcoder.routing.loadbalance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tpcoder.routing.exception.GlobalExceptionHandler;
import dev.tpcoder.routing.exception.NoHostAvailableException;
import dev.tpcoder.routing.exception.UpstreamException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoadBalancerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrafficDistributable roundRobinLoadBalancer;

    @InjectMocks
    private LoadBalancerController loadBalancerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loadBalancerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnSuccessResponse() throws Exception {
        JsonNode responseBody = objectMapper.readTree("{\"transactionId\":\"TXN123\"}");
        when(roundRobinLoadBalancer.distributeTraffic(any(JsonNode.class))).thenReturn(responseBody);

        mockMvc.perform(post("/lb/round-robin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionId\":\"TXN123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN123"));
    }

    @Test
    void shouldReturn503WhenNoHostAvailable() throws Exception {
        when(roundRobinLoadBalancer.distributeTraffic(any(JsonNode.class)))
                .thenThrow(new NoHostAvailableException("No healthy hosts available"));

        mockMvc.perform(post("/lb/round-robin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionId\":\"TXN123\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void shouldReturn502WhenUpstreamFails() throws Exception {
        when(roundRobinLoadBalancer.distributeTraffic(any(JsonNode.class)))
                .thenThrow(new UpstreamException("Failed to route request to hosts"));

        mockMvc.perform(post("/lb/round-robin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transactionId\":\"TXN123\"}"))
                .andExpect(status().isBadGateway());
    }
}