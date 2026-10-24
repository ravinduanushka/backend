package com.project.bookingservice.controller;

import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.PageResponse;
import com.project.bookingservice.dto.ReviewRequest;
import com.project.bookingservice.dto.ReviewResponse;
import com.project.bookingservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/bookings/{id}/review")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(CurrentUser current, @PathVariable String id,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(current, id, request);
    }

    @GetMapping("/api/reviews")
    public PageResponse<ReviewResponse> list(CurrentUser current,
                                             @RequestParam(required = false) String mentorId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return reviewService.list(mentorId, page, size);
    }
}