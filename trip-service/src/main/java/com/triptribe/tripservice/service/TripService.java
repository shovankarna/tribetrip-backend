package com.triptribe.tripservice.service;

import com.triptribe.tripservice.dto.CreateTripRequest;
import com.triptribe.tripservice.dto.TripMemberResponse;
import com.triptribe.tripservice.dto.TripResponse;
import com.triptribe.tripservice.entity.TripRole;

import java.util.List;

public interface TripService {

    TripResponse createTrip(CreateTripRequest request, String userId);

    List<TripResponse> getUserTrips(String userId);

    TripResponse getTrip(String tripId, String userId);

    List<TripMemberResponse> getTripMembers(String tripId, String userId);

    TripMemberResponse addMember(String tripId, String userIdToAdd, TripRole role, String requesterId);

    void removeMember(String tripId, String userIdToRemove, String requesterId);

    TripResponse updateTrip(String tripId, com.triptribe.tripservice.dto.UpdateTripRequest request, String requesterId);

    void deleteTrip(String tripId, String requesterId);

    TripMemberResponse updateMemberRole(String tripId, String userId, TripRole newRole, String requesterId);

    TripResponse updateTripStatus(String tripId, com.triptribe.tripservice.entity.TripStatus status,
            String requesterId);
}
