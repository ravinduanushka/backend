package com.project.userservice.config;

import com.project.userservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}