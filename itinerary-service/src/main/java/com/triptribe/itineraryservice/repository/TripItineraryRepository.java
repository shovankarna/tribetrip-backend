package com.triptribe.itineraryservice.repository;

import com.triptribe.itineraryservice.entity.TripItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripItineraryRepository extends JpaRepository<TripItinerary, UUID> {
    Optional<TripItinerary> findByTripId(String tripId);
}
