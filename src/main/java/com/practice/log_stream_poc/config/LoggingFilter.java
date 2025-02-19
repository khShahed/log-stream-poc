package com.practice.log_stream_poc.config;

import com.practice.log_stream_poc.controller.ExceptionHandlingController;
import com.practice.log_stream_poc.kafka.LogProducer;
import com.practice.log_stream_poc.model.entity.ApiLog;
import com.practice.log_stream_poc.model.entity.RequestTiming;
import com.practice.log_stream_poc.service.JwtService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Log4j2
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final LogProducer logProducer;
    private final ExceptionHandlingController exceptionHandlingController;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper((HttpServletRequest) request);
        }

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper((HttpServletResponse) response);
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader("Authorization");
        String username = null;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer "
            username = jwtService.getUsernameFromToken(token);
        }

        var apiLog = ApiLog.builder()
            .requestId(UUID.randomUUID().toString())
            .method(httpRequest.getMethod())
            .url(httpRequest.getRequestURI())
            .username(username)
            .requestTiming(
                RequestTiming.builder()
                    .requestStartTime(LocalDateTime.now())
                    .build()
            )
            .build();

        MDC.clear();
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            MDC.put("error", e.getMessage());
            throw e;
        } finally {
            response.setHeader("X-Request-Id", apiLog.getRequestId());
            ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;
            log.info("IsStreamingResposne: {}", isStreamingResponse(wrappedResponse));

            wrappedResponse.copyBodyToResponse();
            response.flushBuffer();
            logRequestResponse((ContentCachingRequestWrapper) httpRequest, wrappedResponse, apiLog);
        }
        MDC.clear();
    }

    private void logRequestResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, ApiLog logEntry)
        throws IOException {
        if (logEntry != null) {
            var timing = logEntry.getRequestTiming();
            timing.setRequestEndTime(LocalDateTime.now());
            timing.setTimeTakenMs(Duration.between(timing.getRequestStartTime(), timing.getRequestEndTime()).toMillis());

            logEntry.setResponseStatus(response.getStatus());
            logEntry.setRequestBody(getRequestBody(request));
            if (isJsonResponse(response)) {
                logEntry.setResponseBody(getResponseBody(response));
            }
            if (MDC.get("error") != null) {
                logEntry.setErrorReason(MDC.get("error"));
            }
            logEntry.setRequestTiming(timing);
            logEntry.setTimestamp(Instant.now());

            logProducer.sendLog(logEntry);
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        return content.length > 0 ? new String(content, StandardCharsets.UTF_8) : "";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) throws IOException {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            response.copyBodyToResponse(); // Ensures the response body is not lost
            return body;
        }
        return "";
    }

    private boolean isJsonResponse(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private String truncateIfNeeded(String body, int maxLength) {
        if (body.length() > maxLength) {
            log.warn("Body truncated. Original length: {}, Max length: {}", body.length(), maxLength);
            return body.substring(0, maxLength) + "...(truncated)";
        }
        return body;
    }

    private boolean isStreamingResponse(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        return contentType != null
            && contentType.contains("application/json") // Keep old compatibility
            && response.getHeader("Transfer-Encoding") != null
            && "chunked".equalsIgnoreCase(response.getHeader("Transfer-Encoding"));
    }
}