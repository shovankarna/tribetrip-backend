package com.triptribe.itineraryservice.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserContextFilter extends OncePerRequestFilter {

    private final UserContext userContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String roles = request.getHeader("X-User-Roles");

        if (userId != null) {
            userContext.setUserId(userId);
            userContext.setEmail(email);
            userContext.setRoles(roles);
        }

        filterChain.doFilter(request, response);
    }
}
