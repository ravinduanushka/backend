package com.project.bookingservice.entity;

import com.project.bookingservice.util.TimeUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "mentoring_sessions")
public class MentoringSession {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "booking_id", nullable = false, unique = true, length = 36)
    private String bookingId;

    @Column(name = "meeting_url")
    private String meetingUrl;

    @Column(name = "meeting_provider")
    private String meetingProvider;

    @Column(name = "actual_start")
    private String actualStart;

    @Column(name = "actual_end")
    private String actualEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        String now = TimeUtil.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = TimeUtil.now();
    }

    public String getId() { return id; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }
    public String getMeetingProvider() { return meetingProvider; }
    public void setMeetingProvider(String meetingProvider) { this.meetingProvider = meetingProvider; }
    public String getActualStart() { return actualStart; }
    public void setActualStart(String actualStart) { this.actualStart = actualStart; }
    public String getActualEnd() { return actualEnd; }
    public void setActualEnd(String actualEnd) { this.actualEnd = actualEnd; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}
