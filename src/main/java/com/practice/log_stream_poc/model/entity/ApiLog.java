package com.practice.log_stream_poc.model.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "api_logs")
@NoArgsConstructor
@AllArgsConstructor
public class ApiLog implements Serializable {
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
