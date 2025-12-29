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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
                    java.util.Map<String, Object> claims = objectMapper.readValue(payload, java.util.Map.class);

                    String userId = (String) claims.get("sub");
                    String email = (String) claims.get("email");
                    // Roles extraction might be complex depending on Keycloak mapper
                    // (realm_access.roles)
                    // For now, let's just get userId as that is critical.
                    // Roles often in realm_access -> roles

                    if (userId != null) {
                        userContext.setUserId(userId);
                        userContext.setEmail(email);
                        // userContext.setRoles(...); // Implement if needed later
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to parse JWT in Itinerary Service", e);
            }
        }

        // Fallback to headers if provided (e.g. by Gateway)
        if (userContext.getUserId() == null) {
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                userContext.setUserId(userId);
                userContext.setEmail(request.getHeader("X-User-Email"));
                userContext.setRoles(request.getHeader("X-User-Roles"));
            }
        }

        filterChain.doFilter(request, response);
    }
}
