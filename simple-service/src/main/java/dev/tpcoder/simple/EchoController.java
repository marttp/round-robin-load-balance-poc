package dev.tpcoder.simple;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/echo")
public class EchoController {

    @PostMapping
    public JsonNode echo(@RequestBody JsonNode payload) {
        return payload;
    }
}
