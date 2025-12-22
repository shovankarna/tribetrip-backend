package com.triptribe.userservice.mapper;

import com.triptribe.userservice.dto.UserProfileDTO;
import com.triptribe.userservice.entity.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between UserProfile entities and UserProfileDTOs.
 */
@Component
public class UserProfileMapper {

    /**
     * Converts a UserProfile entity to a UserProfileDTO.
     *
     * @param entity The UserProfile entity.
     * @return The corresponding UserProfileDTO, or null if entity is null.
     */
    public UserProfileDTO toDTO(UserProfile entity) {
        if (entity == null) {
            return null;
        }
        return UserProfileDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .active(entity.isActive())
                .build();
    }

    /**
     * Converts a UserProfileDTO to a UserProfile entity.
     * Note: This creates a new entity instance. For updates, typically you'd copy
     * fields to an existing entity.
     *
     * @param dto The UserProfileDTO.
     * @return The corresponding UserProfile entity, or null if dto is null.
     */
    public UserProfile toEntity(UserProfileDTO dto) {
        if (dto == null) {
            return null;
        }
        // Using no-args constructor + setters to avoid long constructor issues/changes
        UserProfile entity = new UserProfile();
        entity.setId(dto.getId());
        entity.setEmail(dto.getEmail());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setActive(dto.isActive());
        // createdAt and updatedAt are typically handled by PrePersist/PreUpdate or
        // existing values
        return entity;
    }
}
