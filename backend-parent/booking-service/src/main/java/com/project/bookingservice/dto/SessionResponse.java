package com.project.bookingservice.dto;

import com.project.bookingservice.entity.SessionStatus;

public record SessionResponse(String id, String bookingId, String meetingUrl, String meetingProvider,
                              String actualStart, String actualEnd, SessionStatus status) {
}