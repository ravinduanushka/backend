package com.project.mentorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExpertiseRequest(@NotBlank @Size(max = 100) String name, String description) {
}