package com.project.bookingservice.controller;

import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.SessionResponse;
import com.project.bookingservice.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings/{id}/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public SessionResponse get(CurrentUser current, @PathVariable String id) {
        return sessionService.get(current, id);
    }

    @PostMapping("/start")
    public SessionResponse start(CurrentUser current, @PathVariable String id) {
        return sessionService.start(current, id);
    }

    @PostMapping("/complete")
    public SessionResponse complete(CurrentUser current, @PathVariable String id) {
        return sessionService.complete(current, id);
    }
}