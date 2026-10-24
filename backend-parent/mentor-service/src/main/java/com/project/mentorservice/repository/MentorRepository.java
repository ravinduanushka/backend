package com.project.mentorservice.repository;

import com.project.mentorservice.entity.Mentor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MentorRepository extends JpaRepository<Mentor, String>, JpaSpecificationExecutor<Mentor> {

    Optional<Mentor> findByUserId(String userId);
}