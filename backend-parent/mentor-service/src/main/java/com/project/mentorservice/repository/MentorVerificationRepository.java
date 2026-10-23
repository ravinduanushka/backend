package com.project.mentorservice.repository;

import com.project.mentorservice.entity.MentorVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorVerificationRepository extends JpaRepository<MentorVerification, String> {
}