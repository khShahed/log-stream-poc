package com.practice.log_stream_poc.controller;


import com.practice.log_stream_poc.model.dto.CountResponse;
import com.practice.log_stream_poc.service.ApiLogService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> createGridRatesForMetersBulk(
        @RequestParam(defaultValue = "10") int count) { // Simulate a number of streamed items

        StreamingResponseBody responseStream = outputStream -> {
            try {
                for (int i = 1; i <= count; i++) {
                    String data = "{\"index\":" + i + ", \"message\": \"Streaming response\"}\n";
                    outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
//                    Thread.sleep(200); // Simulate delay in streaming
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
