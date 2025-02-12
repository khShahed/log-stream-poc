package com.practice.log_stream_poc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.log_stream_poc.model.entity.ApiLog;
import com.practice.log_stream_poc.model.entity.RequestTiming;
import com.practice.log_stream_poc.service.ApiLogService;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@RequiredArgsConstructor
public class LoggingFilter implements Filter {
    private final ApiLogService apiLogService;
    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper((HttpServletRequest) request);
        }

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper((HttpServletResponse) response);
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract user info from JWT (assuming it's in the Authorization header)
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
        httpRequest.setAttribute("requestLog", apiLog);

        chain.doFilter(request, response);

        logRequestResponse((ContentCachingRequestWrapper) httpRequest, (ContentCachingResponseWrapper) httpResponse);
    }

    private void logRequestResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response)
        throws IOException {
        ApiLog logEntry = (ApiLog) request.getAttribute("requestLog");

        if (logEntry != null) {
            response.setHeader("X-Request-Id", logEntry.getRequestId());
            var timing = logEntry.getRequestTiming();
            timing.setRequestEndTime(LocalDateTime.now());
            timing.setTimeTakenMs(Duration.between(timing.getRequestStartTime(), timing.getRequestEndTime()).toMillis());

            logEntry.setResponseStatus(response.getStatus());
            logEntry.setRequestBody(getRequestBody(request));
            if (isJsonResponse(response)) {
                logEntry.setResponseBody(getResponseBody(response));
            }
            logEntry.setRequestTiming(timing);

            apiLogService.saveLog(logEntry);
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}