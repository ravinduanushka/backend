package com.project.mentorshipservice.config;

import com.project.mentorshipservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}