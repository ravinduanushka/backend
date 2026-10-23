package com.project.mentorservice.repository;

import com.project.mentorservice.entity.ExpertiseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseCategoryRepository extends JpaRepository<ExpertiseCategory, String> {

    boolean existsByName(String name);
}