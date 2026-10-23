package com.project.userservice.service;

import com.project.userservice.config.CurrentUser;
import com.project.userservice.dto.RegisterRequest;
import com.project.userservice.dto.UserResponse;
import com.project.userservice.entity.Role;
import com.project.userservice.entity.StudentProfile;
import com.project.userservice.entity.User;
import com.project.userservice.exception.ApiException;
import com.project.userservice.repository.StudentProfileRepository;
import com.project.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public UserService(UserRepository userRepository, StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional
    public UserResponse register(CurrentUser current, RegisterRequest request) {
        if (userRepository.existsById(current.id()) || userRepository.existsByEmail(current.email())) {
            throw ApiException.conflict("User already registered");
        }
        User user = new User();
        user.setId(current.id());
        user.setExternalAuthId(current.id());
        user.setEmail(current.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole(Role.valueOf(request.role()));
        // use the returned entity, it has the timestamps
        User saved = userRepository.save(user);

        if (saved.getRole() == Role.STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUserId(saved.getId());
            studentProfileRepository.save(profile);
        }
        return UserResponse.from(saved);
    }
}
