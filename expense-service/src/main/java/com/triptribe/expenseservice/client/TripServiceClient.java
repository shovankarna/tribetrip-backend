package com.triptribe.expenseservice.client;

import com.triptribe.expenseservice.config.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.config.trip-service.url}")
    private String tripServiceUrl;

    public void validateTripAndMember(String tripId, String userId) {
        // We can call /api/internal/trips/{tripId}/permissions?userId={userId}
        // or check membership endpoint.
        // Assuming TripService has an endpoint exposed.
        // Based on previous context, TripService has `getTripPermission`.
        // Let's assume we can hit that or a simple "getTrip" as validation.

        // For MVP, if we run in docker, `trip-service` hostname resolves.
        // API: GET /api/trips/{tripId}
        // Header: X-User-Id: {userId}

        String url = tripServiceUrl + "/api/trips/" + tripId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);

        String token = UserContext.getAuthToken();
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, entity, Void.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return;
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Trip not found or user is not a member");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new IllegalArgumentException("User is not a member of the trip");
        } catch (Exception e) {
            log.error("Error calling TripService", e);
            throw new RuntimeException("Underlying service error: " + e.getMessage());
        }
    }
}
