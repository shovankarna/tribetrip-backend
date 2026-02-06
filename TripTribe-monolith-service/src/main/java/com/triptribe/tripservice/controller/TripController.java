package com.triptribe.tripservice.controller;

import com.triptribe.tripservice.config.UserContext;
import com.triptribe.tripservice.dto.AddMemberRequest;
import com.triptribe.tripservice.dto.CreateTripRequest;
import com.triptribe.tripservice.dto.UpdateTripRequest;
import com.triptribe.tripservice.dto.TripMemberResponse;
import com.triptribe.tripservice.dto.TripResponse;
import com.triptribe.tripservice.entity.TripRole;
import com.triptribe.tripservice.entity.TripStatus;
import com.triptribe.tripservice.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow all for MVP dev
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(@RequestBody CreateTripRequest request) {
        String userId = UserContext.getUserId();
        // Validation of request body if needed
        return tripService.createTrip(request, userId);
    }

    @GetMapping
    public List<TripResponse> getUserTrips() {
        String userId = UserContext.getUserId();
        return tripService.getUserTrips(userId);
    }

    @GetMapping("/{tripId}")
    public TripResponse getTrip(@PathVariable String tripId) {
        String userId = UserContext.getUserId();
        return tripService.getTrip(tripId, userId);
    }

    @PutMapping("/{tripId}")
    public TripResponse updateTrip(@PathVariable String tripId, @RequestBody UpdateTripRequest request) {
        String userId = UserContext.getUserId();
        return tripService.updateTrip(tripId, request, userId);
    }

    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable String tripId) {
        String userId = UserContext.getUserId();
        tripService.deleteTrip(tripId, userId);
    }

    @GetMapping("/{tripId}/members")
    public List<TripMemberResponse> getTripMembers(@PathVariable String tripId) {
        String userId = UserContext.getUserId();
        return tripService.getTripMembers(tripId, userId);
    }

    @PostMapping("/{tripId}/members")
    public TripMemberResponse addMember(@PathVariable String tripId, @RequestBody AddMemberRequest request) {
        String requesterId = UserContext.getUserId();
        return tripService.addMember(tripId, request.getUserId(), request.getRole(), requesterId);
    }

    @DeleteMapping("/{tripId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable String tripId, @PathVariable String userId) {
        String requesterId = UserContext.getUserId();
        tripService.removeMember(tripId, userId, requesterId);
    }

    @PutMapping("/{tripId}/members/{userId}/role")
    public TripMemberResponse updateMemberRole(
            @PathVariable String tripId,
            @PathVariable String userId,
            @RequestBody UpdateRoleRequest request) { // Need DTO? Or just use String/Enum directly?
        // Usually DTO is cleaner. Let's make a tiny local DTO or use RequestParam?
        // RequestBody is standard.
        String requesterId = UserContext.getUserId();
        return tripService.updateMemberRole(tripId, userId, request.getRole(), requesterId);
    }

    @PutMapping("/{tripId}/status")
    public TripResponse updateTripStatus(
            @PathVariable String tripId,
            @RequestBody UpdateStatusRequest request) {
        String requesterId = UserContext.getUserId();
        return tripService.updateTripStatus(tripId, request.getStatus(), requesterId);
    }

    // Inner DTOs for simple updates (or create separate files if preferred, but
    // inner is fine for MVP)
    @lombok.Data
    static class UpdateRoleRequest {
        private TripRole role;
    }

    @lombok.Data
    static class UpdateStatusRequest {
        private TripStatus status;
    }
}
