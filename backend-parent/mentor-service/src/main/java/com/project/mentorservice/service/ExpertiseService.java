package com.project.mentorservice.service;

import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.CategoryRequest;
import com.project.mentorservice.dto.CategoryResponse;
import com.project.mentorservice.dto.ExpertiseItem;
import com.project.mentorservice.dto.ExpertiseRequest;
import com.project.mentorservice.dto.ExpertiseResponse;
import com.project.mentorservice.entity.Expertise;
import com.project.mentorservice.entity.ExpertiseCategory;
import com.project.mentorservice.entity.Role;
import com.project.mentorservice.exception.ApiException;
import com.project.mentorservice.repository.ExpertiseCategoryRepository;
import com.project.mentorservice.repository.ExpertiseRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpertiseService {

    private final ExpertiseCategoryRepository categoryRepository;
    private final ExpertiseRepository expertiseRepository;

    public ExpertiseService(ExpertiseCategoryRepository categoryRepository, ExpertiseRepository expertiseRepository) {
        this.categoryRepository = categoryRepository;
        this.expertiseRepository = expertiseRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        Map<String, List<ExpertiseItem>> byCategory = expertiseRepository.findAll().stream()
                .sorted(Comparator.comparing(Expertise::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(Expertise::getCategoryId,
                        Collectors.mapping(e -> new ExpertiseItem(e.getId(), e.getName(), e.getDescription()),
                                Collectors.toList())));
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                        byCategory.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CurrentUser current, CategoryRequest request) {
        requireAdmin(current);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("Category name already exists");
        }
        ExpertiseCategory category = new ExpertiseCategory();
        category.setName(name);
        category.setDescription(request.description());
        ExpertiseCategory saved = categoryRepository.saveAndFlush(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getDescription(), List.of());
    }

    @Transactional
    public ExpertiseResponse createExpertise(CurrentUser current, String categoryId, ExpertiseRequest request) {
        requireAdmin(current);
        if (!categoryRepository.existsById(categoryId)) {
            throw ApiException.notFound("Category not found");
        }
        String name = request.name().trim();
        if (expertiseRepository.existsByCategoryIdAndNameIgnoreCase(categoryId, name)) {
            throw ApiException.conflict("Expertise name already exists in this category");
        }
        Expertise expertise = new Expertise();
        expertise.setCategoryId(categoryId);
        expertise.setName(name);
        expertise.setDescription(request.description());
        Expertise saved = expertiseRepository.saveAndFlush(expertise);
        return new ExpertiseResponse(saved.getId(), saved.getCategoryId(), saved.getName(), saved.getDescription());
    }

    private void requireAdmin(CurrentUser current) {
        if (current.role() != Role.ADMIN) {
            throw ApiException.forbidden("Only admins can access this");
        }
    }
}