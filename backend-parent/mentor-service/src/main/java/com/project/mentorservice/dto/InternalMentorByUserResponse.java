package com.project.mentorservice.dto;

import com.project.mentorservice.entity.VerificationStatus;

public record InternalMentorByUserResponse(String id, VerificationStatus verificationStatus) {
}