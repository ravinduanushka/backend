package com.project.mentorservice.dto;

import com.project.mentorservice.entity.Proficiency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MentorExpertisesRequest(@NotNull @Valid List<Item> expertises) {

    public record Item(@NotBlank String expertiseId, @NotNull Proficiency proficiency) {
    }
}