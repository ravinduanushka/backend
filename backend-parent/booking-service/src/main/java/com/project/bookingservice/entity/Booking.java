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
@Table(name = "bookings")
public class Booking {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "student_id", nullable = false, length = 36)
    private String studentId;

    @Column(name = "mentor_id", nullable = false, length = 36)
    private String mentorId;

    @Column(name = "slot_id", nullable = false, length = 36)
    private String slotId;

    @Column(name = "scheduled_start", nullable = false)
    private String scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private String scheduledEnd;

    @Column(name = "topic")
    private String topic;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "rescheduled_from_id", length = 36)
    private String rescheduledFromId;

    @Column(name = "expires_at")
    private String expiresAt;

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
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public String getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; }
    public String getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(String scheduledEnd) { this.scheduledEnd = scheduledEnd; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public String getRescheduledFromId() { return rescheduledFromId; }
    public void setRescheduledFromId(String rescheduledFromId) { this.rescheduledFromId = rescheduledFromId; }
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}