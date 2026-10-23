package com.project.mentorservice.repository;

import com.project.mentorservice.entity.MentorExpertise;
import com.project.mentorservice.entity.MentorExpertiseId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorExpertiseRepository extends JpaRepository<MentorExpertise, MentorExpertiseId> {

    List<MentorExpertise> findByIdMentorId(String mentorId);

    void deleteByIdMentorId(String mentorId);
}