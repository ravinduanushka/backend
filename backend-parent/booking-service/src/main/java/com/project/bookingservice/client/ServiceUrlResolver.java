package com.project.bookingservice.client;

import com.project.bookingservice.exception.ApiException;
import java.util.List;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ServiceUrlResolver {

    private final DiscoveryClient discoveryClient;

    public ServiceUrlResolver(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public String baseUrl(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            throw unavailable(serviceName);
        }
        return instances.get(0).getUri().toString();
    }

    public static ApiException unavailable(String serviceName) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, serviceName + " is not available");
    }
}