package com.project.mentorservice.dto;

import com.project.mentorservice.entity.SlotStatus;
import jakarta.validation.constraints.NotNull;

public record SlotStatusRequest(@NotNull SlotStatus status) {
}