package com.project.userservice.controller;

import com.project.userservice.config.CurrentUser;
import com.project.userservice.dto.PageResponse;
import com.project.userservice.dto.RegisterRequest;
import com.project.userservice.dto.StatusRequest;
import com.project.userservice.dto.UpdateUserRequest;
import com.project.userservice.dto.UserResponse;
import com.project.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(CurrentUser current, @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(current, request));
    }

    @GetMapping("/me")
    public UserResponse me(CurrentUser current) {
        return userService.getMe(current);
    }

    @PutMapping("/me")
    public UserResponse updateMe(CurrentUser current, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateMe(current, request);
    }

    @GetMapping
    public PageResponse<UserResponse> list(CurrentUser current,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) String accountStatus,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return userService.list(current, role, accountStatus, q, page, size);
    }

    @PutMapping("/{id}/status")
    public UserResponse updateStatus(CurrentUser current, @PathVariable String id,
                                     @Valid @RequestBody StatusRequest request) {
        return userService.updateStatus(current, id, request);
    }
}