package com.project.userservice.service;

import com.project.userservice.config.CurrentUser;
import com.project.userservice.dto.StudentProfileRequest;
import com.project.userservice.dto.StudentProfileResponse;
import com.project.userservice.entity.Role;
import com.project.userservice.entity.StudentProfile;
import com.project.userservice.entity.User;
import com.project.userservice.exception.ApiException;
import com.project.userservice.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentProfileService {

    private final UserService userService;
    private final StudentProfileRepository profileRepository;

    public StudentProfileService(UserService userService, StudentProfileRepository profileRepository) {
        this.userService = userService;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public StudentProfileResponse get(CurrentUser current) {
        User user = requireStudent(current);
        return StudentProfileResponse.from(findOrCreate(user));
    }

    @Transactional
    public StudentProfileResponse update(CurrentUser current, StudentProfileRequest request) {
        User user = requireStudent(current);
        StudentProfile profile = findOrCreate(user);
        profile.setUniversity(request.university());
        profile.setFaculty(request.faculty());
        profile.setDegree(request.degree());
        profile.setAcademicYear(request.academicYear());
        profile.setBio(request.bio());
        profile.setInterests(request.interests());
        // flush so updatedAt is set before building the response
        return StudentProfileResponse.from(profileRepository.saveAndFlush(profile));
    }

    private User requireStudent(CurrentUser current) {
        User user = userService.getActiveUser(current.id());
        if (user.getRole() != Role.STUDENT) {
            throw ApiException.forbidden("Only students have a student profile");
        }
        return user;
    }

    private StudentProfile findOrCreate(User user) {
        return profileRepository.findByUserId(user.getId()).orElseGet(() -> {
            StudentProfile created = new StudentProfile();
            created.setUserId(user.getId());
            return profileRepository.save(created);
        });
    }
}