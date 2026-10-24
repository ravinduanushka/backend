package com.project.mentorservice.controller;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.MentorRequest;
import com.project.mentorservice.dto.MentorResponse;
import com.project.mentorservice.service.MentorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    @GetMapping("/me")
    public MentorResponse getMe(CurrentUser current) {
        return mentorService.getMe(current);
    }

    @PutMapping("/me")
    public ResponseEntity<MentorResponse> saveMe(CurrentUser current, @Valid @RequestBody MentorRequest request) {
        MentorService.SaveResult result = mentorService.saveMe(current, request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.mentor());
    }
}