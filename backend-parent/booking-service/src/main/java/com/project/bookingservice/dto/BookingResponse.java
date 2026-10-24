package com.project.bookingservice.dto;

import com.project.bookingservice.entity.BookingStatus;

public record BookingResponse(String id, String studentId, String mentorId, String slotId,
                              String scheduledStart, String scheduledEnd, String topic,
                              int priceCents, String currency, BookingStatus status,
                              String cancellationReason, String rescheduledFromId, String expiresAt,
                              String createdAt, String updatedAt) {
}