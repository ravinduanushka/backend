package com.project.notificationservice.service;

import com.project.notificationservice.dto.CreateNotificationRequest;
import com.project.notificationservice.dto.IdResponse;
import com.project.notificationservice.entity.Notification;
import com.project.notificationservice.exception.ApiException;
import com.project.notificationservice.repository.NotificationRepository;
import com.project.notificationservice.util.TimeUtil;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public IdResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setType(request.type());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setChannel(request.channel());
        notification.setScheduledAt(normalizeScheduledAt(request.scheduledAt()));
        return new IdResponse(notificationRepository.save(notification).getId());
    }

    // Stored in the same format as other timestamps so the scheduler can compare as text
    private static String normalizeScheduledAt(String scheduledAt) {
        if (scheduledAt == null || scheduledAt.isBlank()) {
            return null;
        }
        try {
            return TimeUtil.format(Instant.parse(scheduledAt));
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("scheduledAt must be an ISO-8601 UTC timestamp");
        }
    }
}
