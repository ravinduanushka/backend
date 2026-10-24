package com.project.mentorservice.dto;

import com.project.mentorservice.entity.SlotStatus;

public record SlotResponse(String id, String mentorId, String startTime, String endTime, String timezone,
                           SlotStatus status) {
}