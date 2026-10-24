package com.project.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String timestamp, int status, String error, String message, String path,
                       List<FieldIssue> fieldErrors) {

    public record FieldIssue(String field, String message) {
    }

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    public static ApiError of(HttpStatus status, String message, String path, List<FieldIssue> fieldErrors) {
        return new ApiError(FORMAT.format(Instant.now()), status.value(), status.name(), message, path, fieldErrors);
    }
}
