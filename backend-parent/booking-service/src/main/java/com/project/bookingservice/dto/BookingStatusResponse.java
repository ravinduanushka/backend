package com.project.bookingservice.dto;

import com.project.bookingservice.entity.BookingStatus;

public record BookingStatusResponse(String id, BookingStatus status) {
}