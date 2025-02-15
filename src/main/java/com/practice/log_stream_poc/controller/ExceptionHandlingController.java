package com.practice.log_stream_poc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.log_stream_poc.model.dto.ExceptionResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlingController {

    private static final String CLIENT_ERROR = "client_error";

    private static final String SERVER_ERROR = "server_error";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        return customExceptionHandler(ex, null, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> customExceptionHandler(
        String customMessage, Exception ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info("Handling exception: {}", ex.getMessage());
        final ExceptionResponse response;
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (status.is4xxClientError()) {
            response = new ExceptionResponse(CLIENT_ERROR, customMessage);
        } else {
            response = new ExceptionResponse(SERVER_ERROR, customMessage);
        }
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<Object> customExceptionHandler(
        Exception ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return customExceptionHandler(ex.getMessage(), ex, headers, status, request);
    }
}
