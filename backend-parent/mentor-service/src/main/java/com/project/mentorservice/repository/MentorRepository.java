package com.project.mentorservice.repository;

import com.project.mentorservice.entity.Mentor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, String> {

    Optional<Mentor> findByUserId(String userId);
}