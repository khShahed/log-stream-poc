/*
 * Copyright (c) 2025 Powerledger Pty Ltd. All Rights Reserved.
 */

package com.practice.log_stream_poc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.log_stream_poc.service.TimeService;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@RequiredArgsConstructor
@EnableMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class MongoConfig {

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final TimeService timeService;

    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        // MongoDb supports up to milli seconds precision
        return () -> Optional.of(timeService.getInstantNow());
    }
}
