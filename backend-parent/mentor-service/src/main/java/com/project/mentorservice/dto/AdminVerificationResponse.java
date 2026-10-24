package com.project.mentorservice.dto;

import com.project.mentorservice.entity.VerificationStatus;

public record AdminVerificationResponse(String id, String mentorId, String documentKey, String documentUrl,
                                        VerificationStatus status, String reviewedBy, String reviewNote,
                                        String submittedAt, String reviewedAt) {
}