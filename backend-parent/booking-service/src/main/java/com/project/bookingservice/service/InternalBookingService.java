package com.project.bookingservice.service;

import com.project.bookingservice.client.MentorClient;
import com.project.bookingservice.dto.BookingStatusResponse;
import com.project.bookingservice.dto.InternalBookingResponse;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.entity.MentoringSession;
import com.project.bookingservice.exception.ApiException;
import com.project.bookingservice.repository.BookingRepository;
import com.project.bookingservice.repository.MentoringSessionRepository;
import com.project.bookingservice.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class InternalBookingService {

    private static final Logger log = LoggerFactory.getLogger(InternalBookingService.class);

    private final BookingRepository bookingRepository;
    private final MentoringSessionRepository sessionRepository;
    private final MentorClient mentorClient;
    private final TransactionTemplate transactionTemplate;

    public InternalBookingService(BookingRepository bookingRepository,
                                  MentoringSessionRepository sessionRepository,
                                  MentorClient mentorClient, TransactionTemplate transactionTemplate) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.mentorClient = mentorClient;
        this.transactionTemplate = transactionTemplate;
    }

    public InternalBookingResponse getBooking(String id) {
        Booking b = find(id);
        return new InternalBookingResponse(b.getId(), b.getStudentId(), b.getMentorId(), b.getStatus(),
                b.getPriceCents(), b.getCurrency(), b.getExpiresAt());
    }

    public BookingStatusResponse confirm(String id) {
        Booking booking = find(id);
        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.COMPLETED) {
            return new BookingStatusResponse(booking.getId(), booking.getStatus());
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw ApiException.conflict("Booking is " + booking.getStatus());
        }
        if (booking.getExpiresAt() != null && booking.getExpiresAt().compareTo(TimeUtil.now()) < 0) {
            throw ApiException.conflict("Booking has expired");
        }
        moveSlot(booking.getSlotId(), MentorClient.BOOKED);
        BookingStatus status = transactionTemplate.execute(tx -> {
            Booking current = find(id);
            if (current.getStatus() == BookingStatus.PENDING) {
                current.setStatus(BookingStatus.CONFIRMED);
                current.setExpiresAt(null);
                bookingRepository.saveAndFlush(current);
                if (sessionRepository.findByBookingId(id).isEmpty()) {
                    MentoringSession session = new MentoringSession();
                    session.setBookingId(id);
                    session.setMeetingProvider("JITSI");
                    session.setMeetingUrl("https://meet.jit.si/mentorconnect-" + id.substring(0, 8));
                    sessionRepository.saveAndFlush(session);
                }
            }
            return current.getStatus();
        });
        return new BookingStatusResponse(id, status);
    }

    public BookingStatusResponse paymentFailed(String id) {
        Booking booking = find(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            return new BookingStatusResponse(booking.getId(), booking.getStatus());
        }
        moveSlot(booking.getSlotId(), MentorClient.AVAILABLE);
        BookingStatus status = transactionTemplate.execute(tx -> {
            Booking current = find(id);
            if (current.getStatus() == BookingStatus.PENDING) {
                current.setStatus(BookingStatus.CANCELLED);
                current.setCancellationReason("Payment failed");
                current.setExpiresAt(null);
                bookingRepository.saveAndFlush(current);
            }
            return current.getStatus();
        });
        return new BookingStatusResponse(id, status);
    }

    // a 409 on a PENDING booking means a previous attempt already moved the slot
    private void moveSlot(String slotId, String target) {
        try {
            mentorClient.changeSlotStatus(slotId, target);
        } catch (ApiException e) {
            if (e.getStatus() != HttpStatus.CONFLICT) {
                throw e;
            }
            log.info("Slot {} is already {} or not changeable, continuing", slotId, target);
        }
    }

    private Booking find(String id) {
        return bookingRepository.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
    }
}