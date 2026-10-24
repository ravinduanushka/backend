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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class InternalBookingService {

    private final BookingRepository bookingRepository;
    private final MentoringSessionRepository sessionRepository;
    private final BookingLifecycle lifecycle;
    private final TransactionTemplate transactionTemplate;

    public InternalBookingService(BookingRepository bookingRepository,
                                  MentoringSessionRepository sessionRepository,
                                  BookingLifecycle lifecycle, TransactionTemplate transactionTemplate) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.lifecycle = lifecycle;
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
        lifecycle.moveSlot(booking.getSlotId(), MentorClient.BOOKED);
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
        Booking cancelled = lifecycle.cancel(booking, "Payment failed");
        return new BookingStatusResponse(id, cancelled.getStatus());
    }

    private Booking find(String id) {
        return bookingRepository.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
    }
}