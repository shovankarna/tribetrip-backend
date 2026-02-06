package com.triptribe.tripservice.client;

import com.triptribe.userservice.controller.UserProfileController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final UserProfileController userProfileController;

    /**
     * Checks if a user exists by ID.
     * 
     * @param userId The user ID to check.
     * @return true if user exists, false otherwise.
     */
    public boolean isValidUser(String userId) {
        try {
            return userProfileController.getUser(userId).isPresent();
        } catch (Exception e) {
            // Log and return false or throw based on policy
            System.err.println("User validation failed via direct call: " + e.getMessage());
            // Fail secure
            throw new RuntimeException("Could not verify user existence via direct call.", e);
        }
    }
}
