package com.project.bookingservice.service;

import com.project.bookingservice.client.MentorClient;
import com.project.bookingservice.config.CurrentUser;
import com.project.bookingservice.dto.PageResponse;
import com.project.bookingservice.dto.ReviewRequest;
import com.project.bookingservice.dto.ReviewResponse;
import com.project.bookingservice.entity.Booking;
import com.project.bookingservice.entity.BookingStatus;
import com.project.bookingservice.entity.Review;
import com.project.bookingservice.exception.ApiException;
import com.project.bookingservice.repository.BookingRepository;
import com.project.bookingservice.repository.ReviewRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final MentorClient mentorClient;

    public ReviewService(BookingRepository bookingRepository, ReviewRepository reviewRepository,
                         MentorClient mentorClient) {
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.mentorClient = mentorClient;
    }

    public ReviewResponse create(CurrentUser current, String bookingId, ReviewRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (!booking.getStudentId().equals(current.id())) {
            throw ApiException.forbidden("Only the student of this booking can review it");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw ApiException.conflict("Booking is not COMPLETED");
        }
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw ApiException.conflict("Booking is already reviewed");
        }
        Review review = new Review();
        review.setBookingId(bookingId);
        review.setStudentId(booking.getStudentId());
        review.setMentorId(booking.getMentorId());
        review.setRating(request.rating());
        String comment = request.comment();
        review.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        Review saved;
        try {
            saved = reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("Booking is already reviewed");
        }
        try {
            mentorClient.addRating(booking.getMentorId(), request.rating());
        } catch (RuntimeException e) {
            reviewRepository.delete(saved);
            throw e;
        }
        return toResponse(saved);
    }

    public PageResponse<ReviewResponse> list(String mentorId, int page, int size) {
        if (mentorId == null || mentorId.isBlank()) {
            throw ApiException.badRequest("mentorId is required");
        }
        if (page < 0) {
            throw ApiException.badRequest("page must be 0 or more");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.badRequest("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        Page<Review> result = reviewRepository.findByMentorId(mentorId,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"))));
        List<ReviewResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(r.getId(), r.getBookingId(), r.getStudentId(), r.getMentorId(),
                r.getRating(), r.getComment(), r.getCreatedAt());
    }
}