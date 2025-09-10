package dev.tpcoder.simple;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/echo")
public class EchoController {

    private static final Logger logger = LoggerFactory.getLogger(EchoController.class);

    @PostMapping
    public JsonNode echo(@RequestBody JsonNode payload) {
        logger.info("Echo service received request: {}", payload);
        return payload;
    }
}
