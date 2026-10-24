package com.project.mentorservice.service;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.SlotRequest;
import com.project.mentorservice.dto.SlotResponse;
import com.project.mentorservice.entity.AvailabilitySlot;
import com.project.mentorservice.entity.Mentor;
import com.project.mentorservice.entity.Role;
import com.project.mentorservice.entity.SlotStatus;
import com.project.mentorservice.entity.VerificationStatus;
import com.project.mentorservice.exception.ApiException;
import com.project.mentorservice.repository.AvailabilitySlotRepository;
import com.project.mentorservice.repository.MentorRepository;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotService {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
    private static final int MAX_RANGE_DAYS = 31;

    private final AvailabilitySlotRepository slotRepository;
    private final MentorRepository mentorRepository;

    public SlotService(AvailabilitySlotRepository slotRepository, MentorRepository mentorRepository) {
        this.slotRepository = slotRepository;
        this.mentorRepository = mentorRepository;
    }

    @Transactional
    public SlotResponse create(CurrentUser current, SlotRequest request) {
        requireRole(current, Role.MENTOR);
        Mentor mentor = findOwnMentor(current);
        Instant start = parse(request.startTime(), "startTime");
        Instant end = parse(request.endTime(), "endTime");
        validateTimes(start, end);
        String zone = checkZone(request.timezone());
        String startText = FORMAT.format(start);
        if (slotRepository.existsByMentorIdAndStartTime(mentor.getId(), startText)) {
            throw ApiException.conflict("A slot with this start time already exists");
        }
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setMentorId(mentor.getId());
        slot.setStartTime(startText);
        slot.setEndTime(FORMAT.format(end));
        slot.setTimezone(zone);
        return toResponse(slotRepository.saveAndFlush(slot));
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> listMine(CurrentUser current, SlotStatus status, String from, String to) {
        requireRole(current, Role.MENTOR);
        Mentor mentor = findOwnMentor(current);
        String fromText = from == null ? null : FORMAT.format(parse(from, "from"));
        String toText = to == null ? null : FORMAT.format(parse(to, "to"));
        return slotRepository.findByMentorIdOrderByStartTime(mentor.getId()).stream()
                .filter(s -> status == null || s.getStatus() == status)
                .filter(s -> fromText == null || s.getStartTime().compareTo(fromText) >= 0)
                .filter(s -> toText == null || s.getStartTime().compareTo(toText) <= 0)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SlotResponse update(CurrentUser current, String slotId, SlotRequest request) {
        requireRole(current, Role.MENTOR);
        Mentor mentor = findOwnMentor(current);
        AvailabilitySlot slot = slotRepository.findByIdAndMentorId(slotId, mentor.getId())
                .orElseThrow(() -> ApiException.notFound("Slot not found"));
        SlotStatus newStatus = request.status();
        if (newStatus != null && newStatus != SlotStatus.AVAILABLE && newStatus != SlotStatus.BLOCKED) {
            throw ApiException.badRequest("status must be AVAILABLE or BLOCKED");
        }
        if (slot.getStatus() != SlotStatus.AVAILABLE && slot.getStatus() != SlotStatus.BLOCKED) {
            throw ApiException.conflict("Only AVAILABLE or BLOCKED slots can be changed");
        }
        Instant start = parse(request.startTime(), "startTime");
        Instant end = parse(request.endTime(), "endTime");
        validateTimes(start, end);
        String zone = checkZone(request.timezone());
        String startText = FORMAT.format(start);
        if (slotRepository.existsByMentorIdAndStartTimeAndIdNot(mentor.getId(), startText, slot.getId())) {
            throw ApiException.conflict("A slot with this start time already exists");
        }
        slot.setStartTime(startText);
        slot.setEndTime(FORMAT.format(end));
        slot.setTimezone(zone);
        if (newStatus != null) {
            slot.setStatus(newStatus);
        }
        return toResponse(slotRepository.saveAndFlush(slot));
    }

    @Transactional
    public void delete(CurrentUser current, String slotId) {
        requireRole(current, Role.MENTOR);
        Mentor mentor = findOwnMentor(current);
        AvailabilitySlot slot = slotRepository.findByIdAndMentorId(slotId, mentor.getId())
                .orElseThrow(() -> ApiException.notFound("Slot not found"));
        if (slot.getStatus() == SlotStatus.HELD || slot.getStatus() == SlotStatus.BOOKED) {
            throw ApiException.conflict("Slot is HELD or BOOKED and cannot be deleted");
        }
        slotRepository.delete(slot);
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> listForMentor(CurrentUser current, String mentorId, String from, String to) {
        requireRole(current, Role.STUDENT);
        Instant now = Instant.now();
        Instant fromTime = from == null ? now : parse(from, "from");
        Instant toTime = to == null ? fromTime.plus(30, ChronoUnit.DAYS) : parse(to, "to");
        if (!toTime.isAfter(fromTime)) {
            throw ApiException.badRequest("to must be after from");
        }
        if (Duration.between(fromTime, toTime).compareTo(Duration.ofDays(MAX_RANGE_DAYS)) > 0) {
            throw ApiException.badRequest("Range cannot be longer than " + MAX_RANGE_DAYS + " days");
        }
        Mentor mentor = mentorRepository.findById(mentorId)
                .filter(m -> m.getVerificationStatus() == VerificationStatus.APPROVED)
                .orElseThrow(() -> ApiException.notFound("Mentor not found"));
        Instant effectiveFrom = fromTime.isBefore(now) ? now : fromTime;
        return slotRepository.findByMentorIdAndStatusAndStartTimeBetweenOrderByStartTime(
                        mentor.getId(), SlotStatus.AVAILABLE, FORMAT.format(effectiveFrom), FORMAT.format(toTime))
                .stream().map(this::toResponse).toList();
    }

    private void requireRole(CurrentUser current, Role role) {
        if (current.role() != role) {
            throw ApiException.forbidden("Only " + role.name().toLowerCase() + "s can access this");
        }
    }

    private Mentor findOwnMentor(CurrentUser current) {
        return mentorRepository.findByUserId(current.id())
                .orElseThrow(() -> ApiException.notFound("Mentor profile not created yet"));
    }

    private Instant parse(String text, String field) {
        if (text == null || text.isBlank()) {
            throw ApiException.badRequest(field + " is required");
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest(field + " must be an ISO-8601 UTC timestamp, e.g. 2026-11-05T09:00:00.000Z");
        }
    }

    private void validateTimes(Instant start, Instant end) {
        if (!start.isAfter(Instant.now())) {
            throw ApiException.badRequest("startTime must be in the future");
        }
        if (!end.isAfter(start)) {
            throw ApiException.badRequest("endTime must be after startTime");
        }
    }

    private String checkZone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return "UTC";
        }
        try {
            ZoneId.of(timezone);
            return timezone;
        } catch (DateTimeException e) {
            throw ApiException.badRequest("timezone must be a valid IANA name, e.g. Asia/Colombo");
        }
    }

    private SlotResponse toResponse(AvailabilitySlot s) {
        return new SlotResponse(s.getId(), s.getMentorId(), s.getStartTime(), s.getEndTime(),
                s.getTimezone(), s.getStatus());
    }
}
