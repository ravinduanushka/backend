package com.project.mentorservice.service;

import com.project.mentorservice.client.UserClient;
import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.MentorExpertiseResponse;
import com.project.mentorservice.dto.MentorRequest;
import com.project.mentorservice.dto.MentorResponse;
import com.project.mentorservice.dto.UserSummary;
import com.project.mentorservice.entity.Expertise;
import com.project.mentorservice.entity.ExpertiseCategory;
import com.project.mentorservice.entity.Mentor;
import com.project.mentorservice.entity.MentorExpertise;
import com.project.mentorservice.entity.Role;
import com.project.mentorservice.exception.ApiException;
import com.project.mentorservice.repository.ExpertiseCategoryRepository;
import com.project.mentorservice.repository.ExpertiseRepository;
import com.project.mentorservice.repository.MentorExpertiseRepository;
import com.project.mentorservice.repository.MentorRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentorService {

    public record SaveResult(MentorResponse mentor, boolean created) {
    }

    private final MentorRepository mentorRepository;
    private final MentorExpertiseRepository mentorExpertiseRepository;
    private final ExpertiseRepository expertiseRepository;
    private final ExpertiseCategoryRepository categoryRepository;
    private final UserClient userClient;

    public MentorService(MentorRepository mentorRepository, MentorExpertiseRepository mentorExpertiseRepository,
                         ExpertiseRepository expertiseRepository, ExpertiseCategoryRepository categoryRepository,
                         UserClient userClient) {
        this.mentorRepository = mentorRepository;
        this.mentorExpertiseRepository = mentorExpertiseRepository;
        this.expertiseRepository = expertiseRepository;
        this.categoryRepository = categoryRepository;
        this.userClient = userClient;
    }

    @Transactional(readOnly = true)
    public MentorResponse getMe(CurrentUser current) {
        requireMentor(current);
        Mentor mentor = mentorRepository.findByUserId(current.id())
                .orElseThrow(() -> ApiException.notFound("Mentor profile not created yet"));
        return toResponse(mentor);
    }

    @Transactional
    public SaveResult saveMe(CurrentUser current, MentorRequest request) {
        requireMentor(current);
        Optional<Mentor> existing = mentorRepository.findByUserId(current.id());
        boolean created = existing.isEmpty();
        Mentor mentor = existing.orElseGet(() -> {
            Mentor fresh = new Mentor();
            fresh.setUserId(current.id());
            return fresh;
        });
        mentor.setTitle(request.title());
        mentor.setOrganization(request.organization());
        mentor.setIndustry(request.industry());
        mentor.setBio(request.bio());
        mentor.setExperienceYears(request.experienceYears());
        mentor.setHourlyRateCents(request.hourlyRateCents());
        if (request.currency() != null) {
            mentor.setCurrency(request.currency().toUpperCase(Locale.ROOT));
        }
        // flush so updatedAt is set before building the response
        Mentor saved = mentorRepository.saveAndFlush(mentor);
        return new SaveResult(toResponse(saved), created);
    }

    private void requireMentor(CurrentUser current) {
        if (current.role() != Role.MENTOR) {
            throw ApiException.forbidden("Only mentors can access this");
        }
    }

    private MentorResponse toResponse(Mentor m) {
        UserSummary user = userClient.getSummaries(List.of(m.getUserId())).get(m.getUserId());
        return new MentorResponse(m.getId(), m.getUserId(),
                user == null ? null : user.firstName(),
                user == null ? null : user.lastName(),
                user == null ? null : user.profileImageKey(),
                m.getTitle(), m.getOrganization(), m.getIndustry(), m.getBio(),
                m.getExperienceYears(), m.getHourlyRateCents(), m.getCurrency(),
                m.getRatingAvg(), m.getRatingCount(), m.getVerificationStatus(),
                toExpertiseResponses(m.getId()), m.getCreatedAt(), m.getUpdatedAt());
    }

    private List<MentorExpertiseResponse> toExpertiseResponses(String mentorId) {
        List<MentorExpertise> links = mentorExpertiseRepository.findByIdMentorId(mentorId);
        if (links.isEmpty()) {
            return List.of();
        }
        Map<String, Expertise> expertiseById = expertiseRepository
                .findAllById(links.stream().map(l -> l.getId().getExpertiseId()).toList())
                .stream().collect(Collectors.toMap(Expertise::getId, Function.identity()));
        Map<String, ExpertiseCategory> categoryById = categoryRepository
                .findAllById(expertiseById.values().stream().map(Expertise::getCategoryId).distinct().toList())
                .stream().collect(Collectors.toMap(ExpertiseCategory::getId, Function.identity()));
        return links.stream().map(link -> {
            Expertise e = expertiseById.get(link.getId().getExpertiseId());
            ExpertiseCategory c = categoryById.get(e.getCategoryId());
            return new MentorExpertiseResponse(e.getId(), e.getName(), c.getName(), link.getProficiency());
        }).toList();
    }
}