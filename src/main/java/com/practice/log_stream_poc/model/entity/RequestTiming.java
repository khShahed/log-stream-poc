package com.practice.log_stream_poc.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestTiming {
    private Instant requestStartTime;
    private Instant requestEndTime;
    private long timeTakenMs;
}
