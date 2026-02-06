package com.triptribe.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Data Transfer Object for UserProfile.
 * Used to transfer user data between the controller and the client,
 * decoupling the internal entity structure from the external API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean active;
}
