package com.project.userservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StudentProfileRequest(
        String university,
        String faculty,
        String degree,
        @Min(1) @Max(10) Integer academicYear,
        String bio,
        String interests) {
}