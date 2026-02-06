package com.triptribe.userservice.service.impl;

import com.triptribe.userservice.entity.UserProfile;
import com.triptribe.userservice.repository.UserProfileRepository;
import com.triptribe.userservice.service.UserProfileService;
import com.triptribe.userservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the UserProfileService interface.
 * Handles the business logic for user profile management, including creation,
 * retrieval, and updates.
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository repository;

    /**
     * Retrieves an existing user profile or creates a new one if it doesn't exist.
     * <p>
     * This method first checks if a user with the given ID exists in the database.
     * If found, it returns the existing profile.
     * If not found, it creates a new UserProfile entity with the provided details
     * and saves it.
     * </p>
     *
     * @param userId    The unique identifier of the user. and cannot be null
     * @param email     The email address of the user.
     * @param firstName The first name of the user.
     * @param lastName  The last name of the user.
     * @return The existing or newly created UserProfile.
     * @throws IllegalArgumentException if userId is null.
     */
    @Override
    @Transactional
    public UserProfile getOrCreateProfile(String userId, String email, String firstName, String lastName) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null when fetching or creating a profile.");
        }

        return repository.findById(userId)
                .orElseGet(() -> {
                    // Logic to create a new user profile if one does not exist
                    UserProfile newUser = new UserProfile();
                    newUser.setId(userId);
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setActive(true); // Default active
                    return repository.save(newUser);
                });
    }

    /**
     * Retrieves a user profile by its ID.
     *
     * @param userId The unique identifier of the user.
     * @return An Optional containing the UserProfile if found, or empty otherwise.
     */
    @Override
    public Optional<UserProfile> getUser(String userId) {
        return repository.findById(userId);
    }

    /**
     * Updates an existing user profile with new information.
     * <p>
     * This method retrieves the user by ID. If found, it updates the allowed fields
     * (firstName and lastName) if they are present in the updatedProfile object.
     * The email field is explicitly NOT updated as per business rules.
     * </p>
     *
     * @param userId         The unique identifier of the user to update.
     * @param updatedProfile The UserProfile object containing updated fields.
     * @return The updated UserProfile.
     * @throws RuntimeException         if the user with the given ID is not found.
     * @throws IllegalArgumentException if userId is null.
     */
    @Override
    @Transactional
    public UserProfile updateProfile(String userId, UserProfile updatedProfile) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null when updating a profile.");
        }

        return repository.findById(userId)
                .map(existingUser -> {
                    // Only update fields that are allowed to be changed
                    if (updatedProfile.getFirstName() != null) {
                        existingUser.setFirstName(updatedProfile.getFirstName());
                    }
                    if (updatedProfile.getLastName() != null) {
                        existingUser.setLastName(updatedProfile.getLastName());
                    }
                    // Email is intentionally excluded from updates here
                    return repository.save(existingUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public List<UserProfile> getUsers(List<String> userIds) {
        return repository.findAllById(userIds);
    }
}
