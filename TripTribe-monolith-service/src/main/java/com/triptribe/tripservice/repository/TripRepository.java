package com.triptribe.tripservice.repository;

import com.triptribe.tripservice.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
}
