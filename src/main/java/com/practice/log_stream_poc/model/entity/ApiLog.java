package com.practice.log_stream_poc.model.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "api_logs")
public class ApiLog {
    @Id
    private String id;
    private String requestId;
    private String method;
    private String url;
    private String requestBody;
    private String responseBody;
    private int responseStatus;
    private String username;
    private RequestTiming requestTiming;
}
