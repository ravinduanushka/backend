package com.project.bookingservice.service;

import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.BookingResponse;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.entity.SessionStatus;
import com.project.bookingservice.exception.ApiException;
import com.project.bookingservice.repository.BookingRepository;
import com.project.bookingservice.repository.MentoringSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class CancellationService {

    private final BookingRepository bookingRepository;
    private final MentoringSessionRepository sessionRepository;
    private final BookingAccess access;
    private final BookingLifecycle lifecycle;

    public CancellationService(BookingRepository bookingRepository, MentoringSessionRepository sessionRepository,
                               BookingAccess access, BookingLifecycle lifecycle) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.access = access;
        this.lifecycle = lifecycle;
    }

    public BookingResponse cancel(CurrentUser current, String id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (!access.isStudentOf(booking, current) && !access.isMentorOf(booking, current)) {
            throw ApiException.forbidden("You are not a party to this booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw ApiException.conflict("Booking is " + booking.getStatus() + " and cannot be cancelled");
        }
        boolean started = sessionRepository.findByBookingId(id)
                .filter(s -> s.getStatus() == SessionStatus.IN_PROGRESS)
                .isPresent();
        if (started) {
            throw ApiException.conflict("Session has already started");
        }
        String cleanReason = reason == null || reason.isBlank() ? null : reason.trim();
        return BookingMapper.toResponse(lifecycle.cancel(booking, cleanReason));
    }
}