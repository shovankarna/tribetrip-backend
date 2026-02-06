package com.triptribe.itineraryservice.client;

import com.triptribe.itineraryservice.dto.internal.TripPermissionResponse;
import com.triptribe.itineraryservice.exception.ResourceNotFoundException;
import com.triptribe.itineraryservice.exception.UnauthorizedException;
import com.triptribe.tripservice.controller.internal.TripInternalController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("itineraryTripServiceClient")
@RequiredArgsConstructor
public class TripServiceClient {

    private final TripInternalController tripInternalController;

    public TripPermissionResponse getTripPermissions(String tripId, String userId) {
        try {
            var source = tripInternalController.getTripPermissions(tripId, userId);
            return map(source);
        } catch (com.triptribe.tripservice.exception.ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Trip not found or user is not a member (via Trip Service)");
        } catch (com.triptribe.tripservice.exception.UnauthorizedException e) { // Assuming UnauthorizedException exists
                                                                                // in TripService or similar
            throw new UnauthorizedException("User not authorized for this trip");
        } catch (Exception e) {
            // Check for specific exceptions if TripService uses different ones.
            // Fallback
            throw new RuntimeException("Failed to contact Trip Service: " + e.getMessage(), e);
        }
    }

    private TripPermissionResponse map(com.triptribe.tripservice.dto.internal.TripPermissionResponse source) {
        if (source == null)
            return null;
        return new TripPermissionResponse(
                source.getTripId(),
                source.getRole() != null
                        ? com.triptribe.itineraryservice.entity.TripRole.valueOf(source.getRole().name())
                        : null,
                source.getStatus() != null
                        ? com.triptribe.itineraryservice.entity.TripStatus.valueOf(source.getStatus().name())
                        : null,
                source.getStartDate());
    }
}
