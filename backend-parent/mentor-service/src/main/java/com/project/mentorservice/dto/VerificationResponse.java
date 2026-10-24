package com.project.mentorservice.dto;

import com.project.mentorservice.entity.VerificationStatus;

public record VerificationResponse(String id, String mentorId, String documentKey, VerificationStatus status,
                                   String reviewedBy, String reviewNote, String submittedAt, String reviewedAt) {
}