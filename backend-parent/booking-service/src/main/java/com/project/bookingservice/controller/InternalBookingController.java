package com.project.bookingservice.controller;

import com.project.bookingservice.dto.BookingStatusResponse;
import com.project.bookingservice.dto.InternalBookingResponse;
import com.project.bookingservice.service.InternalBookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/bookings")
public class InternalBookingController {

    private final InternalBookingService internalBookingService;

    public InternalBookingController(InternalBookingService internalBookingService) {
        this.internalBookingService = internalBookingService;
    }

    @GetMapping("/{id}")
    public InternalBookingResponse getBooking(@PathVariable String id) {
        return internalBookingService.getBooking(id);
    }

    @PutMapping("/{id}/confirm")
    public BookingStatusResponse confirm(@PathVariable String id) {
        return internalBookingService.confirm(id);
    }

    @PutMapping("/{id}/payment-failed")
    public BookingStatusResponse paymentFailed(@PathVariable String id) {
        return internalBookingService.paymentFailed(id);
    }
}