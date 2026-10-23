package com.project.userservice.controller;

import com.project.userservice.config.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Temporary, delete after U4
@RestController
@RequestMapping("/api/dev")
public class DevController {

    public record EchoRequest(@NotBlank String name) {
    }

    @GetMapping("/whoami")
    public CurrentUser whoami(CurrentUser user) {
        return user;
    }

    @PostMapping("/echo")
    public EchoRequest echo(@Valid @RequestBody EchoRequest body) {
        return body;
    }
}