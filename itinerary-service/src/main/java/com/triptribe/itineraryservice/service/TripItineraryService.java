package com.triptribe.itineraryservice.service;

import com.triptribe.itineraryservice.dto.itinerary.*;

import java.util.UUID;

public interface TripItineraryService {
    void attachTemplate(String tripId, UUID templateId, String userId);

    TripItineraryResponse getTripItinerary(String tripId, String userId);

    TripItineraryItemResponse addItem(String tripId, CreateTripItineraryItemRequest request, String userId);

    TripItineraryItemResponse updateItem(UUID itemId, UpdateTripItineraryItemRequest request, String userId);

    void moveItem(String tripId, UUID itemId, MoveTripItineraryItemRequest request, String userId);

    void deleteItem(UUID itemId, String userId);
}
