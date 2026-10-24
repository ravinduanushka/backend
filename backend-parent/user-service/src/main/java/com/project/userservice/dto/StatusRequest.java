package com.project.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusRequest(
        @NotBlank
        @Pattern(regexp = "ACTIVE|SUSPENDED", message = "accountStatus must be ACTIVE or SUSPENDED")
        String accountStatus) {
}