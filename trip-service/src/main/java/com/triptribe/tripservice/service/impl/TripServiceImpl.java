package com.triptribe.tripservice.service.impl;

import com.triptribe.tripservice.dto.CreateTripRequest;
import com.triptribe.tripservice.dto.UpdateTripRequest;
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
import com.triptribe.tripservice.dto.internal.TripPermissionResponse;
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
                .filter(TripMember::isActive)
                .map(tripMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TripMemberResponse addMember(String tripId, String userIdToAdd, TripRole role, String requesterId) {
        // Validation: Requester must be active OWNER or ADMIN
        TripMember requester = requireActiveMembership(tripId, requesterId);

        if (requester.getRole() != TripRole.OWNER && requester.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can add members");
        }

        if (requester.getRole() == TripRole.ADMIN && role == TripRole.ADMIN) {
            throw new UnauthorizedException("Admins cannot add other Admins. Only Owner can make someone Admin.");
        }

        // Prevent assigning OWNER role directly
        if (role == TripRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role directly");
        }

        // Validate trip exists
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        validateTripAction(trip, ActionType.ADD_MEMBER);

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
        // OWNER can remove anyone (except self).
        // ADMIN can remove MEMBER.
        // MEMBER can remove themselves (Leave trip).

        boolean isSelfRemoval = userIdToRemove.equals(requesterId);
        boolean isOwner = requester.getRole() == TripRole.OWNER;
        boolean isAdmin = requester.getRole() == TripRole.ADMIN;

        if (isSelfRemoval) {
            if (isOwner) {
                throw new IllegalArgumentException("Owner cannot remove themselves. Transfer ownership first.");
            }
            // Allow self removal for others
        } else {
            // Removing someone else
            if (isOwner) {
                // Owner can remove anyone
            } else if (isAdmin) {
                // Admin can remove ONLY Members (not Admins or Owner)
                TripMember memberToRemove = tripMemberRepository.findByTripIdAndUserId(tripId, userIdToRemove)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
                if (memberToRemove.getRole() == TripRole.OWNER || memberToRemove.getRole() == TripRole.ADMIN) {
                    throw new UnauthorizedException("Admin cannot remove Owner or other Admins");
                }
            } else {
                throw new UnauthorizedException("Only OWNER or ADMIN can remove other members");
            }
        }

        TripMember memberToRemove = tripMemberRepository.findByTripIdAndUserId(tripId, userIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!memberToRemove.isActive()) {
            // Already inactive - idempotent success
            return;
        }

        // Validate Lifecycle
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        // Allow exiting a trip by self always? Or apply logic?
        // Logic says: "Member removal: Restricted in ONGOING/COMPLETED/CANCELLED".
        // But "Leave Trip" might be different.
        // Table says: Members [CONFIRMED: Add only, ONGOING: X, COMPLETED: X,
        // CANCELLED: X].
        // So effectively, once CONFIRMED, you cannot be removed or leave?
        // Realism: If I am CONFIRMED, I can leave (cancel my spot).
        // But User Request says: "ONGOING: Member removal ❌".
        // "CONFIRMED: Members ⚠️ Add only".
        // This implies NO removal in CONFIRMED.
        // I will strictly likely follow the table.
        // "CONFIRMED | Members | ⚠️ Add only" -> implies Remove is blocked.
        // However, self-leaving might be an exception or deemed "Cancellation" of
        // participation.
        // For now, I will STRICTLY BLOCK it as per "Add Only" and "Admin/Owner removal"
        // logic.
        // If users get stuck, we can relax it later.

        validateTripAction(trip, ActionType.REMOVE_MEMBER);

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

    @Override
    @Transactional
    public TripMemberResponse updateMemberRole(String tripId, String userId, TripRole newRole, String requesterId) {
        TripMember requester = requireActiveMembership(tripId, requesterId);

        // Only OWNER can change roles (promote/demote admins)
        if (requester.getRole() != TripRole.OWNER) {
            throw new UnauthorizedException("Only OWNER can change member roles");
        }

        if (newRole == TripRole.OWNER) {
            throw new IllegalArgumentException(
                    "Cannot assign OWNER role via update. Use transfer ownership (not implemented yet).");
        }

        TripMember memberToUpdate = tripMemberRepository.findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (memberToUpdate.getUserId().equals(requesterId)) {
            throw new IllegalArgumentException("Owner cannot change their own role");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        validateTripAction(trip, ActionType.CHANGE_ROLE);

        memberToUpdate.setRole(newRole);
        return tripMapper.toDto(tripMemberRepository.save(memberToUpdate));
    }

    @Override
    @Transactional
    public TripResponse updateTripStatus(String tripId, TripStatus status, String requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        TripMember requester = requireActiveMembership(tripId, requesterId);

        // OWNER or ADMIN can update status
        if (requester.getRole() != TripRole.OWNER && requester.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can update trip status");
        }

        // Check if status change is allowed
        // Table: COMPLETED/CANCELLED -> Status Change ❌
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot change status of a " + trip.getStatus() + " trip.");
        }

        // Also verify target status transitions if needed, but for now just source
        // check.
        // Validate if OWNER is allowed to (already checked role).

        trip.setStatus(status);
        return tripMapper.toDto(tripRepository.save(trip), requester.getRole());
    }

    @Override
    @Transactional
    public TripResponse updateTrip(String tripId, UpdateTripRequest request, String requesterId) {
        // Validation: Required fields check can be done here or via @Valid in
        // Controller
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        // Authorization: Requester must be OWNER
        TripMember requester = requireActiveMembership(tripId, requesterId);
        if (requester.getRole() != TripRole.OWNER) {
            throw new UnauthorizedException("Only OWNER can update the trip");
        }

        validateTripAction(trip, ActionType.MODIFY_DETAILS);

        if (request.getName() != null)
            trip.setName(request.getName());
        if (request.getDescription() != null)
            trip.setDescription(request.getDescription());
        if (request.getDestination() != null)
            trip.setDestination(request.getDestination());
        if (request.getStartDate() != null)
            trip.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)
            trip.setEndDate(request.getEndDate());

        return tripMapper.toDto(tripRepository.save(trip), TripRole.OWNER);
    }

    @Override
    @Transactional
    public void deleteTrip(String tripId, String requesterId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.isActive()) {
            throw new ResourceNotFoundException("Trip is already deleted");
        }

        // Authorization: Requester must be OWNER
        TripMember requester = requireActiveMembership(tripId, requesterId);
        if (requester.getRole() != TripRole.OWNER) {
            throw new UnauthorizedException("Only OWNER can delete the trip");
        }

        // Soft delete trip
        trip.setActive(false);
        tripRepository.save(trip);

        // Optionally soft delete all members?
        // Logic usually implies if trip is inactive, members are implicitly inactive
        // contextually.
        // But let's set them to inactive to be clean.
        List<TripMember> members = tripMemberRepository.findByTripId(tripId);
        members.forEach(m -> m.setActive(false));
        tripMemberRepository.saveAll(members);
    }

    private enum ActionType {
        MODIFY_DETAILS,
        ADD_MEMBER,
        REMOVE_MEMBER,
        CHANGE_ROLE
    }

    private void validateTripAction(Trip trip, ActionType action) {
        TripStatus status = trip.getStatus();

        switch (status) {
            case DRAFT:
            case PLANNING:
                // All actions allowed
                break;

            case CONFIRMED:
                if (action == ActionType.MODIFY_DETAILS) {
                    throw new IllegalArgumentException("Cannot modify trip details in CONFIRMED status.");
                }
                if (action == ActionType.REMOVE_MEMBER) {
                    throw new IllegalArgumentException("Cannot remove members in CONFIRMED status.");
                }
                if (action == ActionType.CHANGE_ROLE) {
                    // Allow role changes in CONFIRMED status (for Owner/Admin to manage team)
                }
                // ADD_MEMBER allowed
                break;

            case ONGOING:
                if (action == ActionType.MODIFY_DETAILS) {
                    throw new IllegalArgumentException("Cannot modify trip details in ONGOING status.");
                }
                if (action == ActionType.ADD_MEMBER) {
                    throw new IllegalArgumentException("Cannot add members in ONGOING status.");
                }
                if (action == ActionType.REMOVE_MEMBER) {
                    throw new IllegalArgumentException("Cannot remove members in ONGOING status.");
                }
                if (action == ActionType.CHANGE_ROLE) {
                    // Allow role changes in ONGOING status
                }
                break;

            case COMPLETED:
            case CANCELLED:
                throw new IllegalArgumentException("Cannot perform " + action + " on a " + status + " trip.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TripPermissionResponse getTripPermission(String tripId, String userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, userId)
                .filter(TripMember::isActive)
                .orElseThrow(() -> new UnauthorizedException("User is not an active member of this trip"));

        return TripPermissionResponse.builder()
                .tripId(trip.getId())
                .role(member.getRole())
                .status(trip.getStatus())
                .startDate(trip.getStartDate())
                .build();
    }
}
