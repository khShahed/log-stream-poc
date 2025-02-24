package com.practice.log_stream_poc.auditing;

import com.practice.log_stream_poc.kafka.LogProducer;
import com.practice.log_stream_poc.model.entity.ApiLog;
import com.practice.log_stream_poc.model.entity.RequestTiming;
import com.practice.log_stream_poc.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Log4j2
@Component
@RequiredArgsConstructor
public class ApiLoggingInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;
    private final LogProducer logProducer;

    @Value("${request-auditing.max-request-body-size:100000}")
    private int maxRequestBodySize;

    @Value("${request-auditing.max-response-body-size:10000}")
    private int maxResponseBodySize;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("In Interceptor preHandle");
        MDC.clear();
        var requestId = request.getHeader("X-Request-ID");
        if (StringUtils.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put("requestId", requestId);
        MDC.put("clientIp", request.getRemoteAddr());
        response.setHeader("X-Request-ID", requestId);
        MDC.put("requestStartTime", Instant.now().toString());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.debug("In Interceptor afterCompletion");
        try {
            if (!(request instanceof ContentCachingRequestWrapper)) {
                request = new ContentCachingRequestWrapper(request);
            }

            if (!(response instanceof ContentCachingResponseWrapper)) {
                response = new ContentCachingResponseWrapper(response);
            }

            String token = request.getHeader("Authorization");
            if (token != null) {
                token = token.substring(7).trim();
                MDC.put("username", jwtService.getUsernameFromToken(token));
            }
            Instant startTime = Instant.parse(MDC.get("requestStartTime"));
            Instant endTime = Instant.now();
            String requestBody = getRequestBody(request);
            String responseBody = getResponseBody(response);
            String errorReason = getErrorReason(ex);

            var apiLog = ApiLog.builder()
                .requestId(UUID.randomUUID().toString())
                .method(request.getMethod())
                .url(request.getRequestURI())
                .username(MDC.get("username"))
                .requestBody(requestBody)
                .responseBody(responseBody)
                .errorReason(errorReason)
                .timestamp(startTime)
                .responseStatus(response.getStatus())
                .clientIp(request.getRemoteAddr())
                .requestTiming(
                    RequestTiming.builder()
                        .requestStartTime(startTime)
                        .requestEndTime(endTime)
                        .timeTakenMs(endTime.toEpochMilli() - startTime.toEpochMilli())
                        .build()
                )
                .build();

            logProducer.sendLog(apiLog);
        } finally {
            MDC.clear();
        }
    }

    private String getErrorReason(Exception ex) {
        if (ex != null) {
            return ex.getMessage();
        }

        return null;
    }

    private String getRequestBody(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            byte[] content = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
            var requestBody = content.length > 0 ? new String(content, StandardCharsets.UTF_8) : "";

            return truncateIfNeeded(requestBody, maxRequestBodySize);
        }
        return "EMPTY_BODY";
    }

    private String getResponseBody(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            byte[] content = ((ContentCachingResponseWrapper) response).getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                return truncateIfNeeded(body, maxResponseBodySize);
            }
        }
        return "EMPTY_BODY";
    }

    private String truncateIfNeeded(String body, int maxLength) {
        if (StringUtils.isNotBlank(body) && body.length() > maxLength) {
            log.debug("Body truncated. Original length: {}, Max length: {}", body.length(), maxLength);
            return body.substring(0, maxLength) + "...(truncated)";
        }

        return body;
    }
}
