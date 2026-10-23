package com.project.mentorservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mentor_expertises")
public class MentorExpertise {

    @EmbeddedId
    private MentorExpertiseId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false)
    private Proficiency proficiency = Proficiency.INTERMEDIATE;

    protected MentorExpertise() {
    }

    public MentorExpertise(String mentorId, String expertiseId, Proficiency proficiency) {
        this.id = new MentorExpertiseId(mentorId, expertiseId);
        this.proficiency = proficiency;
    }

    public MentorExpertiseId getId() { return id; }
    public Proficiency getProficiency() { return proficiency; }
    public void setProficiency(Proficiency proficiency) { this.proficiency = proficiency; }
}