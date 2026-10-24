package com.project.mentorservice.repository;

import com.project.mentorservice.entity.AvailabilitySlot;
import com.project.mentorservice.entity.SlotStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, String> {

    boolean existsByMentorIdAndStartTime(String mentorId, String startTime);

    boolean existsByMentorIdAndStartTimeAndIdNot(String mentorId, String startTime, String id);

    Optional<AvailabilitySlot> findByIdAndMentorId(String id, String mentorId);

    List<AvailabilitySlot> findByMentorIdOrderByStartTime(String mentorId);

    List<AvailabilitySlot> findByMentorIdAndStatusAndStartTimeBetweenOrderByStartTime(
            String mentorId, SlotStatus status, String from, String to);
}