package com.project.bookingservice.repository;

import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, String> {

    Page<Booking> findByStudentId(String studentId, Pageable pageable);

    Page<Booking> findByStudentIdAndStatus(String studentId, BookingStatus status, Pageable pageable);

    Page<Booking> findByMentorId(String mentorId, Pageable pageable);

    Page<Booking> findByMentorIdAndStatus(String mentorId, BookingStatus status, Pageable pageable);

    boolean existsBySlotIdAndStatusNotIn(String slotId, Collection<BookingStatus> statuses);

    List<Booking> findByStatusAndExpiresAtLessThan(BookingStatus status, String expiresAt);
}