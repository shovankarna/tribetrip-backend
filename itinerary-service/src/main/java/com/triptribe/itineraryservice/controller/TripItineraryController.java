package com.triptribe.itineraryservice.controller;

import com.triptribe.itineraryservice.config.UserContext;
import com.triptribe.itineraryservice.dto.itinerary.CreateTripItineraryItemRequest;
import com.triptribe.itineraryservice.dto.itinerary.TripItineraryItemResponse;
import com.triptribe.itineraryservice.dto.itinerary.TripItineraryResponse;
import com.triptribe.itineraryservice.dto.itinerary.UpdateTripItineraryItemRequest;
import com.triptribe.itineraryservice.service.TripItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TripItineraryController {

    private final TripItineraryService itineraryService;
    private final UserContext userContext;

    @PostMapping("/api/trips/{tripId}/itineraries/{templateId}/attach")
    @ResponseStatus(HttpStatus.CREATED)
    public void attachTemplate(
            @PathVariable String tripId,
            @PathVariable UUID templateId) {
        itineraryService.attachTemplate(tripId, templateId, userContext.getUserId());
    }

    @GetMapping("/api/trips/{tripId}/itinerary")
    public TripItineraryResponse getTripItinerary(@PathVariable String tripId) {
        return itineraryService.getTripItinerary(tripId, userContext.getUserId());
    }

    // Add item directly to trip itinerary
    @PostMapping("/api/trips/{tripId}/itinerary/items")
    @ResponseStatus(HttpStatus.CREATED)
    public TripItineraryItemResponse addItem(
            @PathVariable String tripId,
            @RequestBody CreateTripItineraryItemRequest request) {
        return itineraryService.addItem(tripId, request, userContext.getUserId());
    }

    // --- Direct Item Operations ---

    @PutMapping("/api/trip-itinerary-items/{itemId}")
    public TripItineraryItemResponse updateItem(
            @PathVariable UUID itemId,
            @RequestBody UpdateTripItineraryItemRequest request) {
        return itineraryService.updateItem(itemId, request, userContext.getUserId());
    }

    @DeleteMapping("/api/trip-itinerary-items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable UUID itemId) {
        itineraryService.deleteItem(itemId, userContext.getUserId());
    }
}
