package com.practice.log_stream_poc.kafka;

import com.practice.log_stream_poc.model.entity.ApiLog;
import com.practice.log_stream_poc.service.ApiLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final ApiLogService apiLogService;

    @KafkaListener(
        topics = "topic-api-logs",
        groupId = "log-consumer-group",
        containerFactory = "kafkaListenerContainerFactory",
        autoStartup = "true"
    )
    public void consumeLog(@Payload List<ApiLog> logs) {
        log.info("Received {} logs", logs.size());
        apiLogService.saveLogs(logs);
    }
}
