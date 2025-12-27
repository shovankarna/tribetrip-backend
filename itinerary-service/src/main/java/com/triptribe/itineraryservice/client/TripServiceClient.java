package com.triptribe.itineraryservice.client;

import com.triptribe.itineraryservice.dto.internal.TripPermissionResponse;
import com.triptribe.itineraryservice.exception.ResourceNotFoundException;
import com.triptribe.itineraryservice.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TripServiceClient {

    private final RestClient restClient;
    // Hardcoded URL for MVP. In real app, use service discovery or properties.
    private static final String TRIP_SERVICE_URL = "http://trip-service:8082";

    public TripServiceClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(TRIP_SERVICE_URL).build();
    }

    public TripPermissionResponse getTripPermissions(String tripId, String userId) {
        try {
            return restClient.get()
                    .uri("/internal/trips/{tripId}/permissions?userId={userId}", tripId, userId)
                    .retrieve()
                    .body(TripPermissionResponse.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Trip not found or user is not a member (via Trip Service)");
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            throw new UnauthorizedException("User not authorized for this trip");
        } catch (Exception e) {
            // Fallback or generic error
            throw new RuntimeException("Failed to contact Trip Service: " + e.getMessage(), e);
        }
    }
}
