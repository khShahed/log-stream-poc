package com.practice.log_stream_poc.controller;


import com.practice.log_stream_poc.model.dto.CountResponse;
import com.practice.log_stream_poc.service.ApiLogService;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
