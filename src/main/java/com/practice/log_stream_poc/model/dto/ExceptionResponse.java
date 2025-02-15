package com.practice.log_stream_poc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@NonNull
@Data
@Builder
public class ExceptionResponse {

    @JsonProperty("error")
    @NonNull
    private final String errorCode;

    @JsonProperty("error_description")
    @NonNull
    private final String errorMessage;
}
