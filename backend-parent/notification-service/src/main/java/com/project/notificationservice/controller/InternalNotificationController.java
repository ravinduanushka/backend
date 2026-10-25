package com.project.notificationservice.controller;

import com.project.notificationservice.dto.CreateNotificationRequest;
import com.project.notificationservice.dto.IdResponse;
import com.project.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalNotificationController {

    private final NotificationService notificationService;

    public InternalNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.create(request);
    }
}
