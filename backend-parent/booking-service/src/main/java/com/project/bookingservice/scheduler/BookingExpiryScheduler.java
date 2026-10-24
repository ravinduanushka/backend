package com.project.bookingservice.scheduler;

import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.repository.BookingRepository;
import com.project.bookingservice.service.BookingLifecycle;
import com.project.bookingservice.util.TimeUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryScheduler.class);
    private static final String REASON = "Payment not completed in time";

    private final BookingRepository bookingRepository;
    private final BookingLifecycle lifecycle;

    public BookingExpiryScheduler(BookingRepository bookingRepository, BookingLifecycle lifecycle) {
        this.bookingRepository = bookingRepository;
        this.lifecycle = lifecycle;
    }

    @Scheduled(fixedDelayString = "${booking.expiry.check-interval-ms:60000}")
    public void cancelExpiredBookings() {
        List<Booking> expired = bookingRepository.findByStatusAndExpiresAtLessThan(
                BookingStatus.PENDING, TimeUtil.now());
        for (Booking candidate : expired) {
            try {
                // re-read so a booking confirmed a moment ago is not cancelled
                Booking fresh = bookingRepository.findById(candidate.getId()).orElse(null);
                if (fresh == null || fresh.getStatus() != BookingStatus.PENDING
                        || fresh.getExpiresAt() == null || fresh.getExpiresAt().compareTo(TimeUtil.now()) >= 0) {
                    continue;
                }
                lifecycle.cancel(fresh, REASON);
                log.info("Expired booking {} cancelled", fresh.getId());
            } catch (RuntimeException e) {
                log.warn("Could not cancel expired booking {}: {}", candidate.getId(), e.getMessage());
            }
        }
    }
}
