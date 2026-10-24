package com.project.mentorservice.dto;

import com.project.mentorservice.entity.SlotStatus;
import jakarta.validation.constraints.NotBlank;

public record SlotRequest(@NotBlank String startTime, @NotBlank String endTime, String timezone,
                          SlotStatus status) {
}