package com.project.mentorservice.repository;

import com.project.mentorservice.entity.MentorVerification;
import com.project.mentorservice.entity.VerificationStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorVerificationRepository extends JpaRepository<MentorVerification, String> {

    boolean existsByMentorIdAndStatus(String mentorId, VerificationStatus status);

    List<MentorVerification> findByMentorIdOrderBySubmittedAtDesc(String mentorId);

    Page<MentorVerification> findByStatus(VerificationStatus status, Pageable pageable);
}