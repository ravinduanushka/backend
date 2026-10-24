package com.project.mentorservice.service;

import com.project.mentorservice.client.UserClient;
import com.project.mentorservice.config.CurrentUser;
import com.project.mentorservice.dto.ExpertiseRef;
import com.project.mentorservice.dto.MentorExpertiseResponse;
import com.project.mentorservice.dto.MentorExpertisesRequest;
import com.project.mentorservice.dto.MentorExpertisesResponse;
import com.project.mentorservice.dto.MentorRequest;
import com.project.mentorservice.dto.MentorResponse;
import com.project.mentorservice.dto.MentorSummary;
import com.project.mentorservice.dto.PageResponse;
import com.project.mentorservice.dto.UserSummary;
import com.project.mentorservice.entity.Expertise;
import com.project.mentorservice.entity.ExpertiseCategory;
import com.project.mentorservice.entity.Mentor;
import com.project.mentorservice.entity.MentorExpertise;
import com.project.mentorservice.entity.Role;
import com.project.mentorservice.entity.VerificationStatus;
import com.project.mentorservice.exception.ApiException;
import com.project.mentorservice.repository.ExpertiseCategoryRepository;
import com.project.mentorservice.repository.ExpertiseRepository;
import com.project.mentorservice.repository.MentorExpertiseRepository;
import com.project.mentorservice.repository.MentorRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentorService {

    private static final int MAX_PAGE_SIZE = 100;

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

    @Transactional
    public MentorExpertisesResponse saveExpertises(CurrentUser current, MentorExpertisesRequest request) {
        requireMentor(current);
        Mentor mentor = mentorRepository.findByUserId(current.id())
                .orElseThrow(() -> ApiException.notFound("Mentor profile not created yet"));
        Set<String> ids = new HashSet<>();
        for (MentorExpertisesRequest.Item item : request.expertises()) {
            if (!ids.add(item.expertiseId())) {
                throw ApiException.badRequest("Duplicate expertiseId: " + item.expertiseId());
            }
        }
        if (expertiseRepository.findAllById(ids).size() != ids.size()) {
            throw ApiException.badRequest("Unknown expertiseId in the list");
        }
        mentorExpertiseRepository.deleteByIdMentorId(mentor.getId());
        // flush the delete first, so re-adding the same expertise does not hit the primary key
        mentorExpertiseRepository.flush();
        for (MentorExpertisesRequest.Item item : request.expertises()) {
            mentorExpertiseRepository.save(
                    new MentorExpertise(mentor.getId(), item.expertiseId(), item.proficiency()));
        }
        mentorExpertiseRepository.flush();
        return new MentorExpertisesResponse(toExpertiseResponses(mentor.getId()));
    }

    @Transactional(readOnly = true)
    public MentorResponse getById(CurrentUser current, String id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Mentor not found"));
        boolean approved = mentor.getVerificationStatus() == VerificationStatus.APPROVED;
        boolean owner = mentor.getUserId().equals(current.id());
        if (!approved && !owner && current.role() != Role.ADMIN) {
            throw ApiException.notFound("Mentor not found");
        }
        return toResponse(mentor);
    }

    @Transactional(readOnly = true)
    public PageResponse<MentorSummary> search(CurrentUser current, String q, String industry, String expertiseId,
                                              Double minRating, int page, int size) {
        if (current.role() != Role.STUDENT) {
            throw ApiException.forbidden("Only students can access this");
        }
        if (page < 0) {
            throw ApiException.badRequest("page must be 0 or more");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.badRequest("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (minRating != null && (minRating < 0 || minRating > 5)) {
            throw ApiException.badRequest("minRating must be between 0 and 5");
        }

        Specification<Mentor> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("verificationStatus"), VerificationStatus.APPROVED));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("organization")), like)));
            }
            if (industry != null && !industry.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("industry")), industry.trim().toLowerCase(Locale.ROOT)));
            }
            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<Double>get("ratingAvg"), minRating));
            }
            if (expertiseId != null && !expertiseId.isBlank()) {
                Subquery<String> sub = query.subquery(String.class);
                Root<MentorExpertise> link = sub.from(MentorExpertise.class);
                sub.select(link.get("id").get("mentorId"))
                        .where(cb.equal(link.get("id").get("expertiseId"), expertiseId));
                predicates.add(root.get("id").in(sub));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Order.desc("ratingAvg"), Sort.Order.desc("ratingCount"), Sort.Order.asc("id"));
        Page<Mentor> result = mentorRepository.findAll(spec, PageRequest.of(page, size, sort));
        List<Mentor> mentors = result.getContent();

        Map<String, UserSummary> users = userClient.getSummaries(
                mentors.stream().map(Mentor::getUserId).distinct().toList());
        Map<String, List<ExpertiseRef>> expertises = expertiseRefs(mentors.stream().map(Mentor::getId).toList());

        List<MentorSummary> content = mentors.stream().map(m -> {
            UserSummary user = users.get(m.getUserId());
            return new MentorSummary(m.getId(),
                    user == null ? null : user.firstName(),
                    user == null ? null : user.lastName(),
                    user == null ? null : user.profileImageKey(),
                    m.getTitle(), m.getOrganization(), m.getIndustry(),
                    m.getExperienceYears(), m.getHourlyRateCents(), m.getCurrency(),
                    m.getRatingAvg(), m.getRatingCount(),
                    expertises.getOrDefault(m.getId(), List.of()));
        }).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private void requireMentor(CurrentUser current) {
        if (current.role() != Role.MENTOR) {
            throw ApiException.forbidden("Only mentors can access this");
        }
    }

    private Map<String, List<ExpertiseRef>> expertiseRefs(List<String> mentorIds) {
        if (mentorIds.isEmpty()) {
            return Map.of();
        }
        List<MentorExpertise> links = mentorExpertiseRepository.findByIdMentorIdIn(mentorIds);
        if (links.isEmpty()) {
            return Map.of();
        }
        Map<String, Expertise> expertiseById = expertiseRepository
                .findAllById(links.stream().map(l -> l.getId().getExpertiseId()).distinct().toList())
                .stream().collect(Collectors.toMap(Expertise::getId, Function.identity()));
        Map<String, List<ExpertiseRef>> result = new HashMap<>();
        for (MentorExpertise link : links) {
            Expertise e = expertiseById.get(link.getId().getExpertiseId());
            result.computeIfAbsent(link.getId().getMentorId(), k -> new ArrayList<>())
                    .add(new ExpertiseRef(e.getId(), e.getName()));
        }
        return result;
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