package com.project.userservice.dto;

import com.project.userservice.entity.User;

public record UserSummaryResponse(String id, String firstName, String lastName, String profileImageKey) {

    public static UserSummaryResponse from(User u) {
        return new UserSummaryResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getProfileImageKey());
    }
}