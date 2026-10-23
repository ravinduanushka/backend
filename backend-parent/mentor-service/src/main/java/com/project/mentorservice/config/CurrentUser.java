package com.project.mentorservice.config;

import com.project.mentorservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}