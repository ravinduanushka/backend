package com.project.userservice.service;

import com.project.userservice.dto.InternalUserResponse;
import com.project.userservice.dto.UserSummaryResponse;
import com.project.userservice.exception.ApiException;
import com.project.userservice.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternalUserService {

    private static final int MAX_IDS = 100;

    private final UserRepository userRepository;

    public InternalUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public InternalUserResponse getById(String id) {
        return userRepository.findById(id)
                .map(InternalUserResponse::from)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getSummaries(String ids) {
        List<String> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        if (idList.isEmpty()) {
            throw ApiException.badRequest("ids must not be empty");
        }
        if (idList.size() > MAX_IDS) {
            throw ApiException.badRequest("Maximum " + MAX_IDS + " ids allowed");
        }
        return userRepository.findAllById(idList).stream().map(UserSummaryResponse::from).toList();
    }
}