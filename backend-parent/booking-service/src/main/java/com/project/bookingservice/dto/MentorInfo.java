package com.project.bookingservice.dto;

public record MentorInfo(String id, String userId, String verificationStatus, int hourlyRateCents,
                         String currency) {
}