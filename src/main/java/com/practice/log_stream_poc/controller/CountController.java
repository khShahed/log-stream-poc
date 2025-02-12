package com.practice.log_stream_poc.controller;


import com.practice.log_stream_poc.model.dto.CountResponse;
import com.practice.log_stream_poc.service.ApiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/count")
public class CountController {
    private final ApiLogService apiLogService;

    @GetMapping("/latest")
    public ResponseEntity<CountResponse> count() {
        return ResponseEntity.ok(new CountResponse(apiLogService.count()));
    }
}
