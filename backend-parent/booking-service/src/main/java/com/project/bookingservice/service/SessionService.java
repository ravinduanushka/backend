package com.project.bookingservice.service;

import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.SessionResponse;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.entity.MentoringSession;
import com.project.bookingservice.entity.SessionStatus;
import com.project.bookingservice.exception.ApiException;
import com.project.bookingservice.repository.BookingRepository;
import com.project.bookingservice.repository.MentoringSessionRepository;
import com.project.bookingservice.util.TimeUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class SessionService {

    private static final long EARLY_START_MINUTES = 15;

    private final BookingRepository bookingRepository;
    private final MentoringSessionRepository sessionRepository;
    private final BookingAccess access;
    private final TransactionTemplate transactionTemplate;

    public SessionService(BookingRepository bookingRepository, MentoringSessionRepository sessionRepository,
                          BookingAccess access, TransactionTemplate transactionTemplate) {
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.access = access;
        this.transactionTemplate = transactionTemplate;
    }

    public SessionResponse get(CurrentUser current, String bookingId) {
        Booking booking = findBooking(bookingId);
        if (!access.isStudentOf(booking, current) && !access.isMentorOf(booking, current)) {
            throw ApiException.forbidden("You are not a party to this booking");
        }
        return toResponse(findSession(bookingId));
    }

    public SessionResponse start(CurrentUser current, String bookingId) {
        Booking booking = findBooking(bookingId);
        requireMentor(booking, current);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw ApiException.conflict("Booking is " + booking.getStatus());
        }
        MentoringSession session = findSession(bookingId);
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw ApiException.conflict("Session is already " + session.getStatus());
        }
        Instant now = Instant.now();
        Instant start = Instant.parse(booking.getScheduledStart());
        Instant end = Instant.parse(booking.getScheduledEnd());
        if (now.isBefore(start.minus(EARLY_START_MINUTES, ChronoUnit.MINUTES))) {
            throw ApiException.conflict("Too early. The session can start " + EARLY_START_MINUTES
                    + " minutes before the scheduled time");
        }
        if (!now.isBefore(end)) {
            throw ApiException.conflict("The session time is over");
        }
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setActualStart(TimeUtil.now());
        return toResponse(sessionRepository.saveAndFlush(session));
    }

    public SessionResponse complete(CurrentUser current, String bookingId) {
        Booking booking = findBooking(bookingId);
        requireMentor(booking, current);
        MentoringSession session = findSession(bookingId);
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw ApiException.conflict("Session is not IN_PROGRESS");
        }
        MentoringSession completed = transactionTemplate.execute(tx -> {
            MentoringSession s = findSession(bookingId);
            Booking b = findBooking(bookingId);
            s.setStatus(SessionStatus.COMPLETED);
            s.setActualEnd(TimeUtil.now());
            b.setStatus(BookingStatus.COMPLETED);
            bookingRepository.saveAndFlush(b);
            return sessionRepository.saveAndFlush(s);
        });
        return toResponse(completed);
    }

    private void requireMentor(Booking booking, CurrentUser current) {
        if (!access.isMentorOf(booking, current)) {
            throw ApiException.forbidden("Only the mentor of this booking can do this");
        }
    }

    private Booking findBooking(String id) {
        return bookingRepository.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
    }

    private MentoringSession findSession(String bookingId) {
        return sessionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> ApiException.notFound("Session not found, the booking is not paid yet"));
    }

    private SessionResponse toResponse(MentoringSession s) {
        return new SessionResponse(s.getId(), s.getBookingId(), s.getMeetingUrl(), s.getMeetingProvider(),
                s.getActualStart(), s.getActualEnd(), s.getStatus());
    }
}