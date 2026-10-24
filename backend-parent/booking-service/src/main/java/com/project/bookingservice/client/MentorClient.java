package com.project.bookingservice.client;

import com.project.bookingservice.dto.MentorByUserInfo;
import com.project.bookingservice.dto.MentorInfo;
import com.project.bookingservice.dto.RatingInfo;
import com.project.bookingservice.dto.SlotInfo;
import com.project.bookingservice.exception.ApiException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MentorClient {

    public static final String HELD = "HELD";
    public static final String BOOKED = "BOOKED";
    public static final String AVAILABLE = "AVAILABLE";

    private static final Logger log = LoggerFactory.getLogger(MentorClient.class);
    private static final String SERVICE = "mentor-service";

    private final RestClient restClient;
    private final ServiceUrlResolver resolver;

    public MentorClient(RestClient restClient, ServiceUrlResolver resolver) {
        this.restClient = restClient;
        this.resolver = resolver;
    }

    public Optional<MentorInfo> getMentor(String id) {
        String base = resolver.baseUrl(SERVICE);
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(base + "/internal/mentors/{id}", id)
                    .retrieve()
                    .body(MentorInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw failed(e);
        }
    }

    public Optional<MentorByUserInfo> getMentorByUser(String userId) {
        String base = resolver.baseUrl(SERVICE);
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(base + "/internal/mentors/by-user/{userId}", userId)
                    .retrieve()
                    .body(MentorByUserInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw failed(e);
        }
    }

    public SlotInfo changeSlotStatus(String slotId, String status) {
        String base = resolver.baseUrl(SERVICE);
        try {
            return restClient.put()
                    .uri(base + "/internal/slots/{slotId}/status", slotId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("status", status))
                    .retrieve()
                    .body(SlotInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.notFound("Slot not found");
        } catch (HttpClientErrorException.Conflict e) {
            throw ApiException.conflict("Slot is not available");
        } catch (HttpClientErrorException.BadRequest e) {
            throw ApiException.badRequest("Invalid slot status");
        } catch (RestClientException e) {
            throw failed(e);
        }
    }

    public RatingInfo addRating(String mentorId, int rating) {
        String base = resolver.baseUrl(SERVICE);
        try {
            return restClient.post()
                    .uri(base + "/internal/mentors/{id}/rating", mentorId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("rating", rating))
                    .retrieve()
                    .body(RatingInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.notFound("Mentor not found");
        } catch (HttpClientErrorException.BadRequest e) {
            throw ApiException.badRequest("Invalid rating");
        } catch (RestClientException e) {
            throw failed(e);
        }
    }

    private ApiException failed(RestClientException e) {
        log.warn("mentor-service call failed: {}", e.getMessage());
        return ServiceUrlResolver.unavailable(SERVICE);
    }
}