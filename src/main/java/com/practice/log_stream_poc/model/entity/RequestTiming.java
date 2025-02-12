package com.practice.log_stream_poc.model.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestTiming {
    private LocalDateTime requestStartTime;
    private LocalDateTime requestEndTime;
    private long timeTakenMs;
}
