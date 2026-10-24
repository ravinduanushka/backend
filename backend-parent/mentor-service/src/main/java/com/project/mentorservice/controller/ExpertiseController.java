package com.project.mentorservice.controller;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.CategoryRequest;
import com.project.mentorservice.dto.CategoryResponse;
import com.project.mentorservice.dto.ExpertiseRequest;
import com.project.mentorservice.dto.ExpertiseResponse;
import com.project.mentorservice.service.ExpertiseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expertise-categories")
public class ExpertiseController {

    private final ExpertiseService expertiseService;

    public ExpertiseController(ExpertiseService expertiseService) {
        this.expertiseService = expertiseService;
    }

    @GetMapping
    public List<CategoryResponse> list(CurrentUser current) {
        return expertiseService.listCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(CurrentUser current, @Valid @RequestBody CategoryRequest request) {
        return expertiseService.createCategory(current, request);
    }

    @PostMapping("/{id}/expertises")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpertiseResponse createExpertise(CurrentUser current, @PathVariable String id,
                                             @Valid @RequestBody ExpertiseRequest request) {
        return expertiseService.createExpertise(current, id, request);
    }
}