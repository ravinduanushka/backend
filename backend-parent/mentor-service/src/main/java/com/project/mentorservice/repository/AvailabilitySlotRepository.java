package com.project.mentorservice.repository;

import com.project.mentorservice.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, String> {
}