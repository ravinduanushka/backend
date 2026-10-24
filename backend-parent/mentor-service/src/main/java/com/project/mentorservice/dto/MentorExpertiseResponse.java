package com.project.mentorservice.dto;

import com.project.mentorservice.entity.Proficiency;

public record MentorExpertiseResponse(String expertiseId, String name, String categoryName,
                                      Proficiency proficiency) {
}