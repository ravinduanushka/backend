package com.project.mentorservice.controller;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.AdminVerificationResponse;
import com.project.mentorservice.dto.PageResponse;
import com.project.mentorservice.dto.VerificationRequest;
import com.project.mentorservice.dto.VerificationResponse;
import com.project.mentorservice.dto.VerificationReviewRequest;
import com.project.mentorservice.entity.VerificationStatus;
import com.project.mentorservice.service.VerificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/mentors/me/verifications")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationResponse submit(CurrentUser current, @Valid @RequestBody VerificationRequest request) {
        return verificationService.submit(current, request);
    }

    @GetMapping("/mentors/me/verifications")
    public List<VerificationResponse> listMine(CurrentUser current) {
        return verificationService.listMine(current);
    }

    @GetMapping("/verifications")
    public PageResponse<AdminVerificationResponse> listForAdmin(CurrentUser current,
                                                                @RequestParam(required = false) VerificationStatus status,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        return verificationService.listForAdmin(current, status, page, size);
    }

    @PutMapping("/verifications/{id}")
    public VerificationResponse review(CurrentUser current, @PathVariable String id,
                                       @Valid @RequestBody VerificationReviewRequest request) {
        return verificationService.review(current, id, request);
    }
}