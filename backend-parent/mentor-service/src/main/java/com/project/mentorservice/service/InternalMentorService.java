package com.project.mentorservice.service;

import com.project.mentorservice.dto.InternalMentorByUserResponse;
import com.project.mentorservice.dto.InternalMentorResponse;
import com.project.mentorservice.dto.InternalSlotResponse;
import com.project.mentorservice.dto.RatingResponse;
import com.project.mentorservice.entity.AvailabilitySlot;
import com.project.mentorservice.entity.Mentor;
import com.project.mentorservice.entity.SlotStatus;
import com.project.mentorservice.exception.ApiException;
import com.project.mentorservice.repository.AvailabilitySlotRepository;
import com.project.mentorservice.repository.MentorRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternalMentorService {

    private final MentorRepository mentorRepository;
    private final AvailabilitySlotRepository slotRepository;

    public InternalMentorService(MentorRepository mentorRepository, AvailabilitySlotRepository slotRepository) {
        this.mentorRepository = mentorRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional(readOnly = true)
    public InternalMentorResponse getMentor(String id) {
        Mentor m = findMentor(id);
        return new InternalMentorResponse(m.getId(), m.getUserId(), m.getVerificationStatus(),
                m.getHourlyRateCents(), m.getCurrency());
    }

    @Transactional(readOnly = true)
    public InternalMentorByUserResponse getMentorByUser(String userId) {
        Mentor m = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Mentor profile not found"));
        return new InternalMentorByUserResponse(m.getId(), m.getVerificationStatus());
    }

    @Transactional
    public InternalSlotResponse changeSlotStatus(String slotId, SlotStatus target) {
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> ApiException.notFound("Slot not found"));
        if (!allowed(slot.getStatus(), target)) {
            throw ApiException.conflict("Slot status change " + slot.getStatus() + " to " + target + " is not allowed");
        }
        slot.setStatus(target);
        try {
            slotRepository.saveAndFlush(slot);
        } catch (OptimisticLockingFailureException e) {
            throw ApiException.conflict("Slot was changed by someone else");
        }
        return new InternalSlotResponse(slot.getId(), slot.getMentorId(), slot.getStartTime(),
                slot.getEndTime(), slot.getStatus());
    }

    @Transactional
    public RatingResponse addRating(String mentorId, int rating) {
        Mentor mentor = findMentor(mentorId);
        int count = mentor.getRatingCount();
        double avg = (mentor.getRatingAvg() * count + rating) / (count + 1);
        mentor.setRatingAvg(Math.round(avg * 100.0) / 100.0);
        mentor.setRatingCount(count + 1);
        Mentor saved = mentorRepository.saveAndFlush(mentor);
        return new RatingResponse(saved.getRatingAvg(), saved.getRatingCount());
    }

    private Mentor findMentor(String id) {
        return mentorRepository.findById(id).orElseThrow(() -> ApiException.notFound("Mentor not found"));
    }

    private static boolean allowed(SlotStatus from, SlotStatus to) {
        return (from == SlotStatus.AVAILABLE && to == SlotStatus.HELD)
                || (from == SlotStatus.HELD && (to == SlotStatus.BOOKED || to == SlotStatus.AVAILABLE))
                || (from == SlotStatus.BOOKED && to == SlotStatus.AVAILABLE);
    }
}