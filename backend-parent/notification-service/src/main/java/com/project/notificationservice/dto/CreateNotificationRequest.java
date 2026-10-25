package com.project.notificationservice.dto;

import com.project.notificationservice.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateNotificationRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "must be a UUID")
        String userId,
        @NotBlank String type,
        @NotBlank String title,
        @NotBlank String message,
        @NotNull NotificationChannel channel,
        String scheduledAt) {
}
