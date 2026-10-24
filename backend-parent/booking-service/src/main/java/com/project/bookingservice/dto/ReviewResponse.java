package com.project.bookingservice.dto;

public record ReviewResponse(String id, String bookingId, String studentId, String mentorId, int rating,
                             String comment, String createdAt) {
}