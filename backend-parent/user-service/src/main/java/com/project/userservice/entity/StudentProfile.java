package com.project.userservice.entity;

import com.project.userservice.util.TimeUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(name = "university")
    private String university;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "degree")
    private String degree;

    @Column(name = "academic_year")
    private Integer academicYear;

    @Column(name = "bio")
    private String bio;

    @Column(name = "interests")
    private String interests;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        updatedAt = TimeUtil.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = TimeUtil.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }
    public String getUpdatedAt() { return updatedAt; }
}