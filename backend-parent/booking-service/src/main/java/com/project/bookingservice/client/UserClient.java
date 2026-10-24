package com.project.bookingservice.client;

import com.project.bookingservice.dto.UserInfo;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);
    private static final String SERVICE = "user-service";

    private final RestClient restClient;
    private final ServiceUrlResolver resolver;

    public UserClient(RestClient restClient, ServiceUrlResolver resolver) {
        this.restClient = restClient;
        this.resolver = resolver;
    }

    public Optional<UserInfo> getUser(String id) {
        String base = resolver.baseUrl(SERVICE);
        try {
            return Optional.ofNullable(restClient.get()
                    .uri(base + "/internal/users/{id}", id)
                    .retrieve()
                    .body(UserInfo.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("user-service call failed: {}", e.getMessage());
            throw ServiceUrlResolver.unavailable(SERVICE);
        }
    }
}