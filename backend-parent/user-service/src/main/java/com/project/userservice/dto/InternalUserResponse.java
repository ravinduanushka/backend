package com.project.userservice.dto;

import com.project.userservice.entity.AccountStatus;
import com.project.userservice.entity.Role;
import com.project.userservice.entity.User;

public record InternalUserResponse(String id, Role role, AccountStatus accountStatus) {

    public static InternalUserResponse from(User u) {
        return new InternalUserResponse(u.getId(), u.getRole(), u.getAccountStatus());
    }
}