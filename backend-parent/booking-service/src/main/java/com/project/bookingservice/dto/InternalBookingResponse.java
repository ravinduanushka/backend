package com.project.bookingservice.dto;

import com.project.bookingservice.entity.BookingStatus;

public record InternalBookingResponse(String id, String studentId, String mentorId, BookingStatus status,
                                      int priceCents, String currency, String expiresAt) {
}