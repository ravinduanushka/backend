package com.project.mentorservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MentorRequest(
        @NotBlank @Size(max = 150) String title,
        String organization,
        String industry,
        String bio,
        @NotNull @Min(0) Integer experienceYears,
        @NotNull @Min(0) Integer hourlyRateCents,
        @Pattern(regexp = "[A-Za-z]{3}", message = "must be 3 letters") String currency) {
}