package com.project.mentorservice.dto;

import com.project.mentorservice.entity.VerificationStatus;

public record InternalMentorResponse(String id, String userId, VerificationStatus verificationStatus,
                                     int hourlyRateCents, String currency) {
}