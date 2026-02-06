package com.triptribe.expenseservice.client;

import com.triptribe.expenseservice.config.UserContext;
import com.triptribe.tripservice.controller.TripController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("expenseTripServiceClient")
@RequiredArgsConstructor
@Slf4j
public class TripServiceClient {

    private final TripController tripController;

    public void validateTripAndMember(String tripId, String userId) {
        // Bridge UserContext from Expense to Trip
        com.triptribe.expenseservice.config.UserContext.UserDetails expenseUser = UserContext.getCurrentUser();
        if (expenseUser != null) {
            com.triptribe.tripservice.config.UserContext.UserDetails tripUser = new com.triptribe.tripservice.config.UserContext.UserDetails(
                    expenseUser.getUserId(),
                    expenseUser.getEmail(),
                    expenseUser.getFirstName(),
                    expenseUser.getLastName(),
                    expenseUser.getUsername());
            com.triptribe.tripservice.config.UserContext.setCurrentUser(tripUser);
        }

        try {
            // Direct call to controller
            tripController.getTrip(tripId);
            // If no exception, it's valid (Controller throws ResourceNotFoundException
            // usually)
        } catch (com.triptribe.tripservice.exception.ResourceNotFoundException e) {
            throw new IllegalArgumentException("Trip not found or user is not a member");
        } catch (Exception e) {
            // Check if it's a permission issue (TripController checks permission)
            // TripController.getTrip throws specific exceptions.
            // If the user is not part of the trip, TripService usually throws NotFound or
            // Forbidden.
            // We'll catch generics.
            log.error("Error calling TripService via Direct Call", e);
            throw new IllegalArgumentException("Trip validation failed: " + e.getMessage());
        } finally {
            com.triptribe.tripservice.config.UserContext.clear();
        }
    }
}
