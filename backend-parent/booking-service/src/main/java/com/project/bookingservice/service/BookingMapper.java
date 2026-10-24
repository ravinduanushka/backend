package com.project.bookingservice.service;

import com.project.bookingservice.dto.BookingResponse;
import com.project.bookingservice.entity.Booking;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getStudentId(), b.getMentorId(), b.getSlotId(),
                b.getScheduledStart(), b.getScheduledEnd(), b.getTopic(), b.getPriceCents(), b.getCurrency(),
                b.getStatus(), b.getCancellationReason(), b.getRescheduledFromId(), b.getExpiresAt(),
                b.getCreatedAt(), b.getUpdatedAt());
    }
}