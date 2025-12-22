package com.triptribe.userservice.controller;

import com.triptribe.userservice.dto.UserProfileDTO;
import com.triptribe.userservice.config.UserContext;
import com.triptribe.userservice.entity.UserProfile;
import com.triptribe.userservice.mapper.UserProfileMapper;
import com.triptribe.userservice.service.UserProfileService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all for MVP dev
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileMapper userProfileMapper;

    @GetMapping("/me")
    public UserProfileDTO getMe() {
        String userId = UserContext.getUserId();
        String email = UserContext.getEmail();
        // For first usage, we might need name which isn't in static accessors yet, or
        // get via getCurrentUser()
        UserContext.UserDetails details = UserContext.getCurrentUser();

        // Safety check if accessed without token (though Filter logs it)
        if (details == null) {
            throw new RuntimeException("User not authenticated"); // Or rely on global handler/filter 401
        }

        UserProfile profile = userProfileService.getOrCreateProfile(
                details.getUserId(),
                details.getEmail(),
                details.getFirstName(),
                details.getLastName());
        return userProfileMapper.toDTO(profile);
    }

    @GetMapping("/{id}")
    public Optional<UserProfileDTO> getUser(@PathVariable String id) {
        return userProfileService.getUser(id)
                .map(userProfileMapper::toDTO);
    }

    @PutMapping("/me")
    public UserProfileDTO updateProfile(@RequestBody UserProfileDTO updatedProfileDTO) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("User context missing");
        }

        UserProfile updatedProfile = userProfileMapper.toEntity(updatedProfileDTO);
        UserProfile savedProfile = userProfileService.updateProfile(userId, updatedProfile);
        return userProfileMapper.toDTO(savedProfile);
    }
}
