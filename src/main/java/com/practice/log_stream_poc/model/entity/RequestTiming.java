package com.practice.log_stream_poc.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestTiming {
    private LocalDateTime requestStartTime;
    private LocalDateTime requestEndTime;
    private long timeTakenMs;
}
