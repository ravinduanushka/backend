package com.project.userservice.controller;

import com.project.userservice.config.CurrentUser;
import com.project.userservice.dto.StudentProfileRequest;
import com.project.userservice.dto.StudentProfileResponse;
import com.project.userservice.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/me/profile")
public class StudentProfileController {

    private final StudentProfileService profileService;

    public StudentProfileController(StudentProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public StudentProfileResponse get(CurrentUser current) {
        return profileService.get(current);
    }

    @PutMapping
    public StudentProfileResponse update(CurrentUser current, @Valid @RequestBody StudentProfileRequest request) {
        return profileService.update(current, request);
    }
}