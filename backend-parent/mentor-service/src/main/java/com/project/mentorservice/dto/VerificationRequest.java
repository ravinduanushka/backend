package com.project.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificationRequest(@NotBlank String documentKey) {
}