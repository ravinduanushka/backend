package com.project.mentorservice.repository;

import com.project.mentorservice.entity.Expertise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseRepository extends JpaRepository<Expertise, String> {

    List<Expertise> findByCategoryId(String categoryId);

    boolean existsByCategoryIdAndName(String categoryId, String name);
}
