package com.project.paymentservice.config;

import com.project.paymentservice.entity.Role;

public record CurrentUser(String id, String email, Role role) {
}