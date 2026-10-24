package com.project.mentorservice.repository;

import com.project.mentorservice.entity.MentorExpertise;
import com.project.mentorservice.entity.MentorExpertiseId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorExpertiseRepository extends JpaRepository<MentorExpertise, MentorExpertiseId> {

    List<MentorExpertise> findByIdMentorId(String mentorId);

    List<MentorExpertise> findByIdMentorIdIn(Collection<String> mentorIds);

    void deleteByIdMentorId(String mentorId);
}