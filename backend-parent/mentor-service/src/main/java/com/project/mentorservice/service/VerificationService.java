package com.project.mentorservice.service;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.AdminVerificationResponse;
import com.project.mentorservice.dto.PageResponse;
import com.project.mentorservice.dto.VerificationRequest;
import com.project.mentorservice.dto.VerificationResponse;
import com.project.mentorservice.dto.VerificationReviewRequest;
import com.project.mentorservice.entity.Mentor;
import com.project.mentorservice.entity.MentorVerification;
import com.project.mentorservice.entity.Role;
import com.project.mentorservice.entity.VerificationStatus;
import com.project.mentorservice.exception.ApiException;
import com.project.mentorservice.repository.MentorRepository;
import com.project.mentorservice.repository.MentorVerificationRepository;
import com.project.mentorservice.util.TimeUtil;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MentorVerificationRepository verificationRepository;
    private final MentorRepository mentorRepository;

    public VerificationService(MentorVerificationRepository verificationRepository,
                               MentorRepository mentorRepository) {
        this.verificationRepository = verificationRepository;
        this.mentorRepository = mentorRepository;
    }

    @Transactional
    public VerificationResponse submit(CurrentUser current, VerificationRequest request) {
        requireRole(current, Role.MENTOR);
        Mentor mentor = findOwnMentor(current);
        String prefix = "verifications/" + mentor.getId() + "/";
        if (!request.documentKey().startsWith(prefix) || request.documentKey().length() == prefix.length()) {
            throw ApiException.badRequest("documentKey must start with " + prefix);
        }
        if (verificationRepository.existsByMentorIdAndStatus(mentor.getId(), VerificationStatus.PENDING)) {
            throw ApiException.conflict("A PENDING verification already exists");
        }
        MentorVerification verification = new MentorVerification();
        verification.setMentorId(mentor.getId());
        verification.setDocumentKey(request.documentKey());
        return toResponse(verificationRepository.saveAndFlush(verification));
    }

    @Transactional(readOnly = true)
    public List<VerificationResponse> listMine(CurrentUser current) {
        requireRole(current, Role.MENTOR);
        Mentor mentor = findOwnMentor(current);
        return verificationRepository.findByMentorIdOrderBySubmittedAtDesc(mentor.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminVerificationResponse> listForAdmin(CurrentUser current, VerificationStatus status,
                                                                int page, int size) {
        requireRole(current, Role.ADMIN);
        if (page < 0) {
            throw ApiException.badRequest("page must be 0 or more");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.badRequest("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("submittedAt")));
        Page<MentorVerification> result = status == null
                ? verificationRepository.findAll(pageable)
                : verificationRepository.findByStatus(status, pageable);
        List<AdminVerificationResponse> content = result.getContent().stream()
                .map(v -> new AdminVerificationResponse(v.getId(), v.getMentorId(), v.getDocumentKey(), null,
                        v.getStatus(), v.getReviewedBy(), v.getReviewNote(), v.getSubmittedAt(), v.getReviewedAt()))
                .toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public VerificationResponse review(CurrentUser current, String id, VerificationReviewRequest request) {
        requireRole(current, Role.ADMIN);
        VerificationStatus decision = request.status();
        if (decision != VerificationStatus.APPROVED && decision != VerificationStatus.REJECTED) {
            throw ApiException.badRequest("status must be APPROVED or REJECTED");
        }
        if (decision == VerificationStatus.REJECTED
                && (request.reviewNote() == null || request.reviewNote().isBlank())) {
            throw ApiException.badRequest("reviewNote is required when REJECTED");
        }
        MentorVerification verification = verificationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Verification not found"));
        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw ApiException.conflict("Verification is already reviewed");
        }
        Mentor mentor = mentorRepository.findById(verification.getMentorId())
                .orElseThrow(() -> ApiException.notFound("Mentor not found"));
        verification.setStatus(decision);
        verification.setReviewNote(request.reviewNote());
        verification.setReviewedBy(current.id());
        verification.setReviewedAt(TimeUtil.now());
        mentor.setVerificationStatus(decision);
        mentorRepository.save(mentor);
        return toResponse(verificationRepository.saveAndFlush(verification));
    }

    private void requireRole(CurrentUser current, Role role) {
        if (current.role() != role) {
            throw ApiException.forbidden("Only " + role.name().toLowerCase() + "s can access this");
        }
    }

    private Mentor findOwnMentor(CurrentUser current) {
        return mentorRepository.findByUserId(current.id())
                .orElseThrow(() -> ApiException.notFound("Mentor profile not created yet"));
    }

    private VerificationResponse toResponse(MentorVerification v) {
        return new VerificationResponse(v.getId(), v.getMentorId(), v.getDocumentKey(), v.getStatus(),
                v.getReviewedBy(), v.getReviewNote(), v.getSubmittedAt(), v.getReviewedAt());
    }
}