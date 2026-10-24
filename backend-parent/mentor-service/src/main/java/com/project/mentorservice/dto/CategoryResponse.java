package com.project.mentorservice.dto;

import java.util.List;

public record CategoryResponse(String id, String name, String description, List<ExpertiseItem> expertises) {
}