package com.project.userservice.service;

import com.project.userservice.config.CurrentUser;
import com.project.userservice.dto.PageResponse;
import com.project.userservice.dto.RegisterRequest;
import com.project.userservice.dto.StatusRequest;
import com.project.userservice.dto.UpdateUserRequest;
import com.project.userservice.dto.UserResponse;
import com.project.userservice.entity.AccountStatus;
import com.project.userservice.entity.Role;
import com.project.userservice.entity.StudentProfile;
import com.project.userservice.entity.User;
import com.project.userservice.exception.ApiException;
import com.project.userservice.repository.StudentProfileRepository;
import com.project.userservice.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Transactional(readOnly = true)
    public UserResponse getMe(CurrentUser current) {
        return UserResponse.from(getActiveUser(current.id()));
    }

    @Transactional
    public UserResponse updateMe(CurrentUser current, UpdateUserRequest request) {
        User user = getActiveUser(current.id());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setProfileImageKey(request.profileImageKey());
        // flush so updatedAt is set before building the response
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(CurrentUser current, String role, String accountStatus,
                                           String q, int page, int size) {
        requireAdmin(current);

        Role roleFilter = null;
        if (role != null && !role.isBlank()) {
            try {
                roleFilter = Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("Invalid role: " + role);
            }
        }
        AccountStatus statusFilter = null;
        if (accountStatus != null && !accountStatus.isBlank()) {
            try {
                statusFilter = AccountStatus.valueOf(accountStatus.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("Invalid accountStatus: " + accountStatus);
            }
        }

        final Role roleValue = roleFilter;
        final AccountStatus statusValue = statusFilter;
        final String text = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (roleValue != null) {
                predicates.add(cb.equal(root.get("role"), roleValue));
            }
            if (statusValue != null) {
                predicates.add(cb.equal(root.get("accountStatus"), statusValue));
            }
            if (text != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), text),
                        cb.like(cb.lower(root.get("firstName")), text),
                        cb.like(cb.lower(root.get("lastName")), text)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<User> result = userRepository.findAll(spec,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.from(result.map(UserResponse::from));
    }

    @Transactional
    public UserResponse updateStatus(CurrentUser current, String id, StatusRequest request) {
        requireAdmin(current);
        if (current.id().equals(id)) {
            throw ApiException.conflict("An admin cannot change their own account status");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        user.setAccountStatus(AccountStatus.valueOf(request.accountStatus()));
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    public User getActiveUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User not registered"));
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw ApiException.forbidden("Account is not active");
        }
        return user;
    }

    private void requireAdmin(CurrentUser current) {
        if (!"ADMIN".equals(String.valueOf(current.role()))) {
            throw ApiException.forbidden("Admin only");
        }
    }
}