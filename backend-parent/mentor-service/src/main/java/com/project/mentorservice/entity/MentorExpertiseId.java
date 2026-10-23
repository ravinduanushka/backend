package com.project.mentorservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MentorExpertiseId implements Serializable {

    @Column(name = "mentor_id", length = 36)
    private String mentorId;

    @Column(name = "expertise_id", length = 36)
    private String expertiseId;

    protected MentorExpertiseId() {
    }

    public MentorExpertiseId(String mentorId, String expertiseId) {
        this.mentorId = mentorId;
        this.expertiseId = expertiseId;
    }

    public String getMentorId() { return mentorId; }
    public String getExpertiseId() { return expertiseId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MentorExpertiseId other)) {
            return false;
        }
        return Objects.equals(mentorId, other.mentorId) && Objects.equals(expertiseId, other.expertiseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mentorId, expertiseId);
    }
}