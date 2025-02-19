/*
 * Copyright (c) 2025 Powerledger Pty Ltd. All Rights Reserved.
 */

package com.practice.log_stream_poc.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimeService {

    public Instant getInstantNow() {
        // MongoDB supports up to milli seconds precision
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
