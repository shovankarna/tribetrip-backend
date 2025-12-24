package com.triptribe.tripservice.controller;

import com.triptribe.tripservice.config.UserContext;
import com.triptribe.tripservice.dto.AddMemberRequest;
import com.triptribe.tripservice.dto.CreateTripRequest;
import com.triptribe.tripservice.dto.TripMemberResponse;
import com.triptribe.tripservice.dto.TripResponse;
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
}
