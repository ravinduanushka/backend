package com.project.bookingservice.service;

import com.project.bookingservice.client.MentorClient;
import com.project.bookingservice.client.UserClient;
import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.BookingRequest;
import com.project.bookingservice.dto.BookingResponse;
import com.project.bookingservice.dto.MentorByUserInfo;
import com.project.bookingservice.dto.MentorInfo;
import com.project.bookingservice.dto.PageResponse;
import com.project.bookingservice.dto.SlotInfo;
import com.project.bookingservice.dto.UserInfo;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.entity.Role;
import com.project.bookingservice.exception.ApiException;
import com.project.bookingservice.repository.BookingRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final int HOLD_MINUTES = 15;
    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private final BookingRepository bookingRepository;
    private final UserClient userClient;
    private final MentorClient mentorClient;
    private final BookingAccess access;

    public BookingService(BookingRepository bookingRepository, UserClient userClient,
                          MentorClient mentorClient, BookingAccess access) {
        this.bookingRepository = bookingRepository;
        this.userClient = userClient;
        this.mentorClient = mentorClient;
        this.access = access;
    }

    public BookingResponse create(CurrentUser current, BookingRequest request) {
        if (current.role() != Role.STUDENT) {
            throw ApiException.forbidden("Only students can book sessions");
        }
        UserInfo user = userClient.getUser(current.id())
                .orElseThrow(() -> ApiException.forbidden("Student account not found"));
        if (!"STUDENT".equals(user.role()) || !"ACTIVE".equals(user.accountStatus())) {
            throw ApiException.forbidden("Only active student accounts can book sessions");
        }

        SlotInfo slot = mentorClient.changeSlotStatus(request.slotId(), MentorClient.HELD);
        try {
            MentorInfo mentor = mentorClient.getMentor(slot.mentorId())
                    .filter(m -> "APPROVED".equals(m.verificationStatus()))
                    .orElseThrow(() -> ApiException.conflict("Mentor is not approved"));
            Instant start = Instant.parse(slot.startTime());
            Instant end = Instant.parse(slot.endTime());
            if (!start.isAfter(Instant.now())) {
                throw ApiException.conflict("Slot has already started");
            }
            long seconds = Duration.between(start, end).getSeconds();
            int price = (int) Math.round(mentor.hourlyRateCents() * seconds / 3600.0);

            Booking booking = new Booking();
            booking.setStudentId(current.id());
            booking.setMentorId(mentor.id());
            booking.setSlotId(slot.id());
            booking.setScheduledStart(slot.startTime());
            booking.setScheduledEnd(slot.endTime());
            booking.setTopic(request.topic());
            booking.setPriceCents(price);
            booking.setCurrency(mentor.currency());
            booking.setStatus(BookingStatus.PENDING);
            booking.setExpiresAt(FORMAT.format(Instant.now().plus(HOLD_MINUTES, ChronoUnit.MINUTES)));
            return toResponse(bookingRepository.saveAndFlush(booking));
        } catch (DataIntegrityViolationException e) {
            releaseSlot(slot.id());
            throw ApiException.conflict("Slot already has an active booking");
        } catch (RuntimeException e) {
            releaseSlot(slot.id());
            throw e;
        }
    }

    public PageResponse<BookingResponse> list(CurrentUser current, BookingStatus status, int page, int size) {
        if (page < 0) {
            throw ApiException.badRequest("page must be 0 or more");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.badRequest("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Booking> result;
        if (current.role() == Role.STUDENT) {
            result = status == null
                    ? bookingRepository.findByStudentId(current.id(), pageable)
                    : bookingRepository.findByStudentIdAndStatus(current.id(), status, pageable);
        } else if (current.role() == Role.MENTOR) {
            Optional<MentorByUserInfo> mentor = mentorClient.getMentorByUser(current.id());
            if (mentor.isEmpty()) {
                return new PageResponse<>(List.of(), page, size, 0, 0);
            }
            result = status == null
                    ? bookingRepository.findByMentorId(mentor.get().id(), pageable)
                    : bookingRepository.findByMentorIdAndStatus(mentor.get().id(), status, pageable);
        } else {
            throw ApiException.forbidden("Only students and mentors can access this");
        }
        List<BookingResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    public BookingResponse getById(CurrentUser current, String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Booking not found"));
        boolean allowed = current.role() == Role.ADMIN
                || access.isStudentOf(booking, current)
                || access.isMentorOf(booking, current);
        if (!allowed) {
            throw ApiException.forbidden("You are not a party to this booking");
        }
        return toResponse(booking);
    }

    private void releaseSlot(String slotId) {
        try {
            mentorClient.changeSlotStatus(slotId, MentorClient.AVAILABLE);
        } catch (RuntimeException e) {
            log.warn("Could not release slot {}: {}", slotId, e.getMessage());
        }
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.getId(), b.getStudentId(), b.getMentorId(), b.getSlotId(),
                b.getScheduledStart(), b.getScheduledEnd(), b.getTopic(), b.getPriceCents(), b.getCurrency(),
                b.getStatus(), b.getCancellationReason(), b.getRescheduledFromId(), b.getExpiresAt(),
                b.getCreatedAt(), b.getUpdatedAt());
    }
}