package com.triptribe.userservice.service;

import com.triptribe.userservice.entity.UserProfile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user profiles.
 * Defines the contract for business operations related to UserProfile.
 */
public interface UserProfileService {

    /**
     * Retrieves an existing user profile or creates a new one if it doesn't exist.
     * This is typically used when a user logs in for the first time or accesses
     * their profile.
     *
     * @param userId    The unique identifier of the user (from Auth provider).
     * @param email     The email address of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     * @return The existing or newly created UserProfile.
     */
    UserProfile getOrCreateProfile(String userId, String email, String firstName, String lastName);

    /**
     * Retrieves a user profile by its ID.
     *
     * @param userId The unique identifier of the user.
     * @return An Optional containing the UserProfile if found, or empty otherwise.
     */
    Optional<UserProfile> getUser(String userId);

    /**
     * Updates an existing user profile with new information.
     * Only allowed fields (like firstName and lastName) should be updated.
     *
     * @param userId         The unique identifier of the user to update.
     * @param updatedProfile The UserProfile object containing updated fields.
     * @return The updated UserProfile.
     * @throws RuntimeException if the user is not found.
     */
    UserProfile updateProfile(String userId, UserProfile updatedProfile);

    /**
     * Retrieves a list of user profiles for the given list of IDs.
     *
     * @param userIds List of user IDs.
     * @return List of UserProfile objects.
     */
    List<UserProfile> getUsers(List<String> userIds);
}
