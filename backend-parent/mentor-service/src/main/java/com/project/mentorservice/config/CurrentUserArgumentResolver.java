package com.project.mentorservice.config;

import com.project.mentorservice.entity.Role;
import com.project.mentorservice.exception.ApiException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

// Dev auth until Phase 5: replace with JWT
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CurrentUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String id = webRequest.getHeader("X-Dev-User-Id");
        String email = webRequest.getHeader("X-Dev-Email");
        String role = webRequest.getHeader("X-Dev-Role");
        if (id == null || email == null || role == null) {
            throw ApiException.unauthorized("Missing X-Dev-User-Id, X-Dev-Email or X-Dev-Role header");
        }
        try {
            UUID.fromString(id);
            return new CurrentUser(id, email, Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            throw ApiException.unauthorized("Invalid X-Dev-User-Id or X-Dev-Role header");
        }
    }
}