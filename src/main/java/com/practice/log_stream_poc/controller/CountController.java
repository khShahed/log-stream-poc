package com.practice.log_stream_poc.controller;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.log_stream_poc.model.dto.CountResponse;
import com.practice.log_stream_poc.service.ApiLogService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/count")
public class CountController {
    private final ApiLogService apiLogService;

    @GetMapping("/error")
    public ResponseEntity<String> getErrorResponse() throws Exception {
        int randomNum = new Random().nextInt(2) + 1;
        if (randomNum == 1) {
            return ResponseEntity.internalServerError().body("Internal server error");
        } else {
            return ResponseEntity.badRequest().body("Bad request");
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<CountResponse> count() {
        return ResponseEntity.ok(new CountResponse(apiLogService.count()));
    }

    @PostMapping("/stream")
    public ResponseEntity<StreamingResponseBody> createGridRatesForMetersBulk(
        InputStream inputStream,
        @RequestParam(defaultValue = "10") int count) {

        var objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = objectMapper.getFactory();

        try (JsonParser jsonParser = jsonFactory.createParser(inputStream)) {
            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new BadRequestException("Expected JSON array");
            }

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode jsonNode = objectMapper.readTree(jsonParser);

                log.info("JsonNode: {}", jsonNode.toString());
            }
        } catch (IOException e) {
            String errorMessage = String.format("Error processing request: %s", e.getMessage());
            log.info(errorMessage);
        }

        StreamingResponseBody responseStream = outputStream -> {
            try {
                for (int i = 1; i <= count; i++) {
                    String data = "{\"index\":" + i + ", \"message\": \"Streaming response\"}\n";
                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }
            } catch (Exception e) {
                throw new RuntimeException("Streaming interrupted", e);
            }
        };

        return ResponseEntity.status(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseStream);
    }
}
