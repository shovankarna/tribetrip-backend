package com.triptribe.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Component("userUserContextFilter")
public class UserContextFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Determine user from JWT (simple payload decoding for MVP)
                String[] chunks = token.split("\\.");
                if (chunks.length > 1) {
                    String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
                    Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

                    UserContext.UserDetails userDetails = new UserContext.UserDetails(
                            (String) claims.get("sub"),
                            (String) claims.get("email"),
                            (String) claims.get("given_name"),
                            (String) claims.get("family_name"),
                            (String) claims.get("preferred_username"));
                    UserContext.setCurrentUser(userDetails);

                    // Maintain Request attributes for compatibility/debugging if needed, or remove.
                    // Keeping them for now in case of other filters reliance, but UserContext is
                    // primary.
                    request.setAttribute("userId", userDetails.getUserId());
                }
            } catch (Exception e) {
                // Ignore parsing errors for now, treat as unauthenticated
                logger.warn("Failed to parse JWT", e);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
