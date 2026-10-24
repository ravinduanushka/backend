package com.project.bookingservice.service;

import com.project.bookingservice.client.MentorClient;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.entity.SessionStatus;
import com.project.bookingservice.exception.ApiException;
import com.project.bookingservice.repository.BookingRepository;
import com.project.bookingservice.repository.MentoringSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class BookingLifecycle {

    private static final Logger log = LoggerFactory.getLogger(BookingLifecycle.class);

    private final BookingRepository bookingRepository;
    private final MentoringSessionRepository sessionRepository;
    private final MentorClient mentorClient;
    private final TransactionTemplate transactionTemplate;

    public BookingLifecycle(BookingRepository bookingRepository, MentoringSessionRepository sessionRepository,
                            MentorClient mentorClient, TransactionTemplate transactionTemplate) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.mentorClient = mentorClient;
        this.transactionTemplate = transactionTemplate;
    }

    // a 409 here means a previous attempt already moved the slot
    public void moveSlot(String slotId, String target) {
        try {
            mentorClient.changeSlotStatus(slotId, target);
        } catch (ApiException e) {
            if (e.getStatus() != HttpStatus.CONFLICT) {
                throw e;
            }
            log.info("Slot {} is already {} or not changeable, continuing", slotId, target);
        }
    }

    public Booking cancel(Booking booking, String reason) {
        moveSlot(booking.getSlotId(), MentorClient.AVAILABLE);
        return transactionTemplate.execute(tx -> {
            Booking current = bookingRepository.findById(booking.getId())
                    .orElseThrow(() -> ApiException.notFound("Booking not found"));
            if (current.getStatus() == BookingStatus.PENDING || current.getStatus() == BookingStatus.CONFIRMED) {
                current.setStatus(BookingStatus.CANCELLED);
                current.setCancellationReason(reason);
                current.setExpiresAt(null);
                bookingRepository.saveAndFlush(current);
                sessionRepository.findByBookingId(current.getId()).ifPresent(session -> {
                    session.setStatus(SessionStatus.CANCELLED);
                    sessionRepository.saveAndFlush(session);
                });
            }
            return current;
        });
    }
}