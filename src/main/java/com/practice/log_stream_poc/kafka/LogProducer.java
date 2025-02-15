package com.practice.log_stream_poc.kafka;

import com.practice.log_stream_poc.model.entity.ApiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class LogProducer {

    private final KafkaTemplate<String, ApiLog> kafkaTemplate;
    private static final String TOPIC = "topic-api-logs";

    public void sendLog(ApiLog log) {
        kafkaTemplate.send(TOPIC, log);
    }
}
