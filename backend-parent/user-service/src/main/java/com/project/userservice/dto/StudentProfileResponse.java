package com.project.userservice.dto;

import com.project.userservice.entity.StudentProfile;

public record StudentProfileResponse(String id, String userId, String university, String faculty, String degree,
                                     Integer academicYear, String bio, String interests, String updatedAt) {

    public static StudentProfileResponse from(StudentProfile p) {
        return new StudentProfileResponse(p.getId(), p.getUserId(), p.getUniversity(), p.getFaculty(),
                p.getDegree(), p.getAcademicYear(), p.getBio(), p.getInterests(), p.getUpdatedAt());
    }
}