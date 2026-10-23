package com.project.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull @Pattern(regexp = "STUDENT|MENTOR", message = "must be STUDENT or MENTOR") String role,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Pattern(regexp = "\\+?[0-9]{7,15}", message = "must be 7 to 15 digits, may start with +") String phone) {
}
