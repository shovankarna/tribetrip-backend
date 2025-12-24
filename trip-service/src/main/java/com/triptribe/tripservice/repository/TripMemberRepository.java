package com.triptribe.tripservice.repository;

import com.triptribe.tripservice.entity.TripMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, String> {
    List<TripMember> findByUserId(String userId);

    List<TripMember> findByTripId(String tripId);

    Optional<TripMember> findByTripIdAndUserId(String tripId, String userId);

    boolean existsByTripIdAndUserIdAndActiveTrue(String tripId, String userId); // Check for duplicates (active only?)
    // Actually duplication rule says "A user cannot appear twice in the same trip".
    // If they are inactive, can they be added again?
    // "Member removal = active = false". "No duplicate memberships allowed".
    // Usually means unique constraint on tripId + userId. I added that constraint
    // in entity.
    // So findAllByTripIdAndUserId would behave essentially as unique check.
}
