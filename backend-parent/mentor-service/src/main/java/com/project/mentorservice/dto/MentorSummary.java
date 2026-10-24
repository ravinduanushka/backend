package com.project.mentorservice.dto;

import java.util.List;

public record MentorSummary(String id, String firstName, String lastName, String profileImageKey,
                            String title, String organization, String industry,
                            int experienceYears, int hourlyRateCents, String currency,
                            double ratingAvg, int ratingCount, List<ExpertiseRef> expertises) {
}