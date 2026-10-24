package com.project.bookingservice.repository;

import com.project.bookingservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, String> {

    boolean existsByBookingId(String bookingId);

    Page<Review> findByMentorId(String mentorId, Pageable pageable);
}