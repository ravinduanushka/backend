package com.project.userservice.dto;

import com.project.userservice.entity.AccountStatus;
import com.project.userservice.entity.Role;
import com.project.userservice.entity.User;

public record UserResponse(String id, String email, String firstName, String lastName, String phone,
                           String profileImageKey, Role role, AccountStatus accountStatus,
                           String createdAt, String updatedAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getPhone(),
                u.getProfileImageKey(), u.getRole(), u.getAccountStatus(), u.getCreatedAt(), u.getUpdatedAt());
    }
}