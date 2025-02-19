package com.practice.log_stream_poc.controller;

import com.practice.log_stream_poc.model.dto.ExceptionResponse;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@Log4j2
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlingController {

    private static final String CLIENT_ERROR = "client_error";

    private static final String SERVER_ERROR = "server_error";

    /**
     * Handles bad requests (400).
     */
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponse> handleBadRequest(Exception ex) {
        return buildResponse(CLIENT_ERROR, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionResponse> handleNotFound(NoSuchElementException ex) {
        return buildResponse(CLIENT_ERROR, "Resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse> handleResponseStatusException(ResponseStatusException ex) {
        return buildResponse(CLIENT_ERROR, ex.getReason(), (HttpStatus) ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildResponse(SERVER_ERROR, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ExceptionResponse> buildResponse(String type, String message, HttpStatus status) {
        MDC.put("error", message);
        return ResponseEntity.status(status).body(new ExceptionResponse(type, message));
    }
}
