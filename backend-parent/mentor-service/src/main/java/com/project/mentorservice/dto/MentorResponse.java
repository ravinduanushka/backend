package com.project.mentorservice.dto;

import com.project.mentorservice.entity.VerificationStatus;
import java.util.List;

public record MentorResponse(String id, String userId, String firstName, String lastName, String profileImageKey,
                             String title, String organization, String industry, String bio,
                             int experienceYears, int hourlyRateCents, String currency,
                             double ratingAvg, int ratingCount, VerificationStatus verificationStatus,
                             List<MentorExpertiseResponse> expertises, String createdAt, String updatedAt) {
}