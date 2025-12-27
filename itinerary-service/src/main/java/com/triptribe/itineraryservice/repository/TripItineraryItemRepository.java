package com.triptribe.itineraryservice.repository;

import com.triptribe.itineraryservice.entity.TripItineraryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TripItineraryItemRepository extends JpaRepository<TripItineraryItem, UUID> {
}
