package com.practice.log_stream_poc.model.entity;

import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "api_logs")
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
    @CompoundIndex(name = "ApiLog_timestamp_username", def = "{'username': 1, 'timestamp': -1}"),
})
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
    private String errorReason;
    private Instant timestamp;
    private RequestTiming requestTiming;
    private String clientIp;

    @CreatedDate
    @Indexed(expireAfter = "2m")  // TTL Index to delete records after 2 minutes
    private Instant createdAt;
}
