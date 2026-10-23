package com.project.userservice.repository;

import com.project.userservice.entity.StudentProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);
}