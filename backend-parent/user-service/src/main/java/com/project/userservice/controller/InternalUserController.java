package com.project.userservice.controller;

import com.project.userservice.dto.InternalUserResponse;
import com.project.userservice.dto.UserSummaryResponse;
import com.project.userservice.service.InternalUserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final InternalUserService internalUserService;

    public InternalUserController(InternalUserService internalUserService) {
        this.internalUserService = internalUserService;
    }

    @GetMapping("/{id}")
    public InternalUserResponse getById(@PathVariable String id) {
        return internalUserService.getById(id);
    }

    @GetMapping
    public List<UserSummaryResponse> getSummaries(@RequestParam String ids) {
        return internalUserService.getSummaries(ids);
    }
}