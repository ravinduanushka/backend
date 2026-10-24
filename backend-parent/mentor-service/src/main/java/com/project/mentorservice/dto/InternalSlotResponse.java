package com.project.mentorservice.dto;

import com.project.mentorservice.entity.SlotStatus;

public record InternalSlotResponse(String id, String mentorId, String startTime, String endTime,
                                   SlotStatus status) {
}