package com.project.analyticservice.config;

import com.project.analyticservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}