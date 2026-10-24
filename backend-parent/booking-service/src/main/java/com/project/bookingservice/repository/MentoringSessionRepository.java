package com.project.bookingservice.repository;

import com.project.bookingservice.entity.MentoringSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentoringSessionRepository extends JpaRepository<MentoringSession, String> {

    Optional<MentoringSession> findByBookingId(String bookingId);
}