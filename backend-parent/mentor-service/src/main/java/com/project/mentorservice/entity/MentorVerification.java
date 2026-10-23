package com.project.mentorservice.entity;

import com.project.mentorservice.util.TimeUtil;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "mentor_verifications")
public class MentorVerification {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "mentor_id", nullable = false, length = 36)
    private String mentorId;

    @Column(name = "document_key", nullable = false)
    private String documentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private String submittedAt;

    @Column(name = "reviewed_at")
    private String reviewedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        submittedAt = TimeUtil.now();
    }

    public String getId() { return id; }
    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }
    public String getDocumentKey() { return documentKey; }
    public void setDocumentKey(String documentKey) { this.documentKey = documentKey; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public String getSubmittedAt() { return submittedAt; }
    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }
}