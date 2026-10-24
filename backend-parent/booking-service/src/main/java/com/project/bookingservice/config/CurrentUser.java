package com.project.bookingservice.config;

import com.project.bookingservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}