package com.project.mentorservice.client;

import com.project.mentorservice.dto.UserSummary;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient = RestClient.create();

    public UserClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    // Returns an empty map if user-service is unreachable
    public Map<String, UserSummary> getSummaries(Collection<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
            if (instances.isEmpty()) {
                log.warn("user-service is not registered in Eureka");
                return Map.of();
            }
            List<UserSummary> users = restClient.get()
                    .uri(instances.get(0).getUri() + "/internal/users?ids={ids}", String.join(",", ids))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<UserSummary>>() {
                    });
            if (users == null) {
                return Map.of();
            }
            return users.stream().collect(Collectors.toMap(UserSummary::id, Function.identity()));
        } catch (RestClientException e) {
            log.warn("Could not reach user-service: {}", e.getMessage());
            return Map.of();
        }
    }
}