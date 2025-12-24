package com.triptribe.tripservice.service.impl;

import com.triptribe.tripservice.dto.CreateTripRequest;
import com.triptribe.tripservice.dto.TripResponse;
import com.triptribe.tripservice.dto.TripMemberResponse;
import com.triptribe.tripservice.entity.Trip;
import com.triptribe.tripservice.entity.TripMember;
import com.triptribe.tripservice.entity.TripRole;
import com.triptribe.tripservice.entity.TripStatus;
import com.triptribe.tripservice.exception.ResourceNotFoundException;
import com.triptribe.tripservice.exception.UnauthorizedException;
import com.triptribe.tripservice.mapper.TripMapper;
import com.triptribe.tripservice.repository.TripMemberRepository;
import com.triptribe.tripservice.repository.TripRepository;
import com.triptribe.tripservice.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripMapper tripMapper;

    @Override
    @Transactional
    public TripResponse createTrip(CreateTripRequest request, String userId) {
        Trip trip = new Trip();
        trip.setName(request.getName());
        trip.setDescription(request.getDescription());
        trip.setDestination(request.getDestination());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setCreatedByUserId(userId);
        trip.setStatus(TripStatus.DRAFT);
        trip.setActive(true);

        trip = tripRepository.save(trip);

        TripMember owner = new TripMember();
        owner.setTripId(trip.getId());
        owner.setUserId(userId);
        owner.setRole(TripRole.OWNER);
        owner.setActive(true);

        tripMemberRepository.save(owner);

        return tripMapper.toDto(trip, TripRole.OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getUserTrips(String userId) {
        List<TripMember> memberships = tripMemberRepository.findByUserId(userId);

        return memberships.stream()
                .filter(TripMember::isActive)
                .map(membership -> {
                    Trip trip = tripRepository.findById(membership.getTripId())
                            .orElse(null);
                    if (trip == null || !trip.isActive())
                        return null;
                    return tripMapper.toDto(trip, membership.getRole());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTrip(String tripId, String userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Security check: Only members can view trip details
        TripMember membership = requireActiveMembership(tripId, userId);

        return tripMapper.toDto(trip, membership.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripMemberResponse> getTripMembers(String tripId, String userId) {
        // Validation: Requester must be a member
        TripMember requester = requireActiveMembership(tripId, userId);

        // Return all members (active only? or all? Usually active for list)
        // Requirements say "Active / inactive" so maybe all.
        // But logic says "Active member" usually.
        // Let's return all found by tripId, and Mapper handles it.
        // We need a repo method for `findAllByTripId`.

        return tripMemberRepository.findByTripId(tripId).stream()
                .map(tripMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TripMemberResponse addMember(String tripId, String userIdToAdd, TripRole role, String requesterId) {
        // Validation: Requester must be active OWNER
        TripMember requester = requireActiveMembership(tripId, requesterId);

        if (requester.getRole() != TripRole.OWNER) {
            throw new UnauthorizedException("Only active OWNER can add members");
        }

        // Prevent assigning OWNER role directly
        if (role == TripRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role directly");
        }

        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip not found");
        }

        // Check if user is already a member
        Optional<TripMember> existingMemberOpt = tripMemberRepository.findByTripIdAndUserId(tripId, userIdToAdd);

        if (existingMemberOpt.isPresent()) {
            TripMember existing = existingMemberOpt.get();
            if (existing.isActive()) {
                throw new IllegalArgumentException("User is already an active member");
            } else {
                // Re-activate and update role
                existing.setActive(true);
                existing.setRole(role);
                return tripMapper.toDto(tripMemberRepository.save(existing));
            }
        }

        TripMember newMember = new TripMember();
        newMember.setTripId(tripId);
        newMember.setUserId(userIdToAdd);
        newMember.setRole(role);
        newMember.setActive(true);

        return tripMapper.toDto(tripMemberRepository.save(newMember));
    }

    @Override
    @Transactional
    public void removeMember(String tripId, String userIdToRemove, String requesterId) {
        TripMember requester = requireActiveMembership(tripId, requesterId);

        // Logic:
        // OWNER can remove others.
        // MEMBER can remove themselves (Leave trip).

        boolean isOwner = requester.getRole() == TripRole.OWNER;
        boolean isSelfRemoval = userIdToRemove.equals(requesterId);

        if (!isOwner && !isSelfRemoval) {
            throw new UnauthorizedException("Only OWNER can remove other members");
        }

        if (isOwner && isSelfRemoval) {
            throw new IllegalArgumentException("Owner cannot remove themselves. Transfer ownership first.");
        }

        TripMember memberToRemove = tripMemberRepository.findByTripIdAndUserId(tripId, userIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!memberToRemove.isActive()) {
            throw new ResourceNotFoundException("Member is already inactive");
        }

        memberToRemove.setActive(false);
        tripMemberRepository.save(memberToRemove);
    }

    private TripMember requireActiveMembership(String tripId, String userId) {
        TripMember member = tripMemberRepository
                .findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new UnauthorizedException("Not a member of this trip"));

        if (!member.isActive()) {
            throw new UnauthorizedException("Inactive member");
        }
        return member;
    }
}
