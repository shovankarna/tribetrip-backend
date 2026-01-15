package com.triptribe.tripservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(RestClient.Builder restClientBuilder,
            @Value("${spring.config.user-service.url}") String userServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
    }

    /**
     * Checks if a user exists by ID.
     * 
     * @param userId The user ID to check.
     * @return true if user exists, false otherwise.
     */
    public boolean isValidUser(String userId) {
        try {
            // Expecting 200 OK with body if found.
            // If User Service returns 404 on not found, catch NotFound.
            // If User Service returns 200 with null/empty body (for Optional), check body.
            String response = restClient.get()
                    .uri("/api/user/{id}", userId)
                    .retrieve()
                    .body(String.class);

            return response != null && !response.isEmpty() && !response.equals("null");
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            // If service is down or other error, assume validation failed for safety
            // or rethrow to block the action with usage error.
            System.err.println("User validation failed: " + e.getMessage());
            // Fail secure: if we can't verify, don't add.
            throw new RuntimeException("Could not verify user existence. Please try again later.");
        }
    }
}
