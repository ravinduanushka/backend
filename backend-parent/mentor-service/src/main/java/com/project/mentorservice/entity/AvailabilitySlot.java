package com.project.mentorservice.entity;

import com.project.mentorservice.util.TimeUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

@Entity
@Table(name = "availability_slots")
public class AvailabilitySlot {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "mentor_id", nullable = false, length = 36)
    private String mentorId;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "timezone", nullable = false)
    private String timezone = "UTC";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

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
    public String getMentorId() { return mentorId; }
    public void setMentorId(String mentorId) { this.mentorId = mentorId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
    public int getVersion() { return version; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}