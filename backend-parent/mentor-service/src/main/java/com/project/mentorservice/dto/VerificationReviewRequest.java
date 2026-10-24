package com.project.mentorservice.dto;

import com.project.mentorservice.entity.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public record VerificationReviewRequest(@NotNull VerificationStatus status, String reviewNote) {
}
