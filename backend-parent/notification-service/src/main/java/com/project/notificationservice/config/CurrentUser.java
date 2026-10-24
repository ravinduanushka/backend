package com.project.notificationservice.config;

import com.project.notificationservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}