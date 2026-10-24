package com.project.bookingservice.controller;

import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.BookingRequest;
import com.project.bookingservice.dto.BookingResponse;
import com.project.bookingservice.dto.PageResponse;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(CurrentUser current, @Valid @RequestBody BookingRequest request) {
        return bookingService.create(current, request);
    }

    @GetMapping
    public PageResponse<BookingResponse> list(CurrentUser current,
                                              @RequestParam(required = false) BookingStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return bookingService.list(current, status, page, size);
    }

    @GetMapping("/{id}")
    public BookingResponse getById(CurrentUser current, @PathVariable String id) {
        return bookingService.getById(current, id);
    }
}