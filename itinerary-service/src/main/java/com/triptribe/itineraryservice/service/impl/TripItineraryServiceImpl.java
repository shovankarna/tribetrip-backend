package com.triptribe.itineraryservice.service.impl;

import com.triptribe.itineraryservice.client.TripServiceClient;
import com.triptribe.itineraryservice.dto.internal.TripPermissionResponse;
import com.triptribe.itineraryservice.dto.itinerary.*;
import com.triptribe.itineraryservice.entity.*;
import com.triptribe.itineraryservice.exception.ResourceNotFoundException;
import com.triptribe.itineraryservice.exception.UnauthorizedException;
import com.triptribe.itineraryservice.mapper.ItineraryMapper;
import com.triptribe.itineraryservice.repository.ItineraryTemplateRepository;
import com.triptribe.itineraryservice.repository.TripItineraryItemRepository;
import com.triptribe.itineraryservice.repository.TripItineraryRepository;
import com.triptribe.itineraryservice.service.TripItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripItineraryServiceImpl implements TripItineraryService {

    private final TripItineraryRepository tripItineraryRepository;
    private final TripItineraryItemRepository itemRepository;
    private final ItineraryTemplateRepository templateRepository;
    private final TripServiceClient tripServiceClient;
    private final ItineraryMapper mapper;

    @Override
    @Transactional
    public void attachTemplate(String tripId, UUID templateId, String userId) {
        // 1. Verify Permission via Trip Service
        TripPermissionResponse permission = tripServiceClient.getTripPermissions(tripId, userId);

        if (permission.getRole() != TripRole.OWNER && permission.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can attach itinerary");
        }

        validateTripStatusForModification(permission.getStatus());

        // 2. Verify one active itinerary per trip constraint
        if (tripItineraryRepository.findByTripId(tripId).isPresent()) {
            throw new IllegalArgumentException(
                    "Trip already has an attached itinerary. Architecture MVP allows only one.");
        }

        // 3. Fetch Template & Validate
        ItineraryTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        if (template.getStatus() != TemplateStatus.ACTIVE) {
            throw new IllegalArgumentException("Template must be ACTIVE to attach");
        }

        // 4. Create Trip Itinerary
        TripItinerary itinerary = new TripItinerary();
        itinerary.setTripId(tripId);
        itinerary.setTemplateId(templateId);
        itinerary.setCreatedByUserId(userId);

        itinerary = tripItineraryRepository.save(itinerary);

        // 5. Copy & Materialize Items
        LocalDate tripStartDate = permission.getStartDate();
        if (tripStartDate == null) {
            // Fallback or error? Logic says use start date. If null (DRAFT trip might not
            // have date?),
            // default to dayOffset from "unscheduled" or just fail?
            // Let's assume trips in PLANNING/CONFIRMED usually have dates.
            // If no start date, mark all as unscheduled?
            // Better to throw error or require start date.
            throw new IllegalArgumentException("Trip must have a start date to attach itinerary");
        }

        for (ItineraryTemplateItem tItem : template.getItems()) {
            LocalDate itemDate = tripStartDate.plusDays(tItem.getDefaultDayOffset());
            TripItineraryItem tripItem = mapper.toTripItem(tItem, itemDate);
            tripItem.setTripItinerary(itinerary);
            itemRepository.save(tripItem);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TripItineraryResponse getTripItinerary(String tripId, String userId) {
        // Verify Member access
        tripServiceClient.getTripPermissions(tripId, userId);
        // If successful, user is member. Non-members throw 403 in client.

        TripItinerary itinerary = tripItineraryRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("No itinerary attached to this trip"));

        return mapper.toDto(itinerary);
    }

    @Override
    @Transactional
    public TripItineraryItemResponse addItem(String tripId, CreateTripItineraryItemRequest request, String userId) {
        TripPermissionResponse permission = tripServiceClient.getTripPermissions(tripId, userId);

        if (permission.getRole() != TripRole.OWNER && permission.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can add items");
        }
        validateTripStatusForModification(permission.getStatus());

        TripItinerary itinerary = tripItineraryRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("No itinerary attached"));

        TripItineraryItem item = mapper.toEntity(request, itinerary);
        return mapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public TripItineraryItemResponse updateItem(UUID itemId, UpdateTripItineraryItemRequest request, String userId) {
        TripItineraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        String tripId = item.getTripItinerary().getTripId();

        TripPermissionResponse permission = tripServiceClient.getTripPermissions(tripId, userId);

        if (permission.getRole() != TripRole.OWNER && permission.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can modify items");
        }
        validateTripStatusForModification(permission.getStatus());

        // Updates
        if (request.getTitle() != null)
            item.setTitle(request.getTitle());
        if (request.getDate() != null)
            item.setDate(request.getDate());
        if (request.getStartTime() != null)
            item.setStartTime(request.getStartTime());
        if (request.getDurationMinutes() != null)
            item.setDurationMinutes(request.getDurationMinutes());
        if (request.getLocationText() != null)
            item.setLocationText(request.getLocationText());
        if (request.getNotes() != null)
            item.setNotes(request.getNotes());
        if (request.getOrderIndex() != null)
            item.setOrderIndex(request.getOrderIndex());
        if (request.getIsUnscheduled() != null)
            item.setUnscheduled(request.getIsUnscheduled());
        if (request.getCompleted() != null)
            item.setCompleted(request.getCompleted());

        if (item.isUnscheduled()) {
            item.setDate(null);
        }

        return mapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(UUID itemId, String userId) {
        TripItineraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        String tripId = item.getTripItinerary().getTripId();
        TripPermissionResponse permission = tripServiceClient.getTripPermissions(tripId, userId);

        if (permission.getRole() != TripRole.OWNER && permission.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can delete items");
        }
        validateTripStatusForModification(permission.getStatus());

        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public void moveItem(String tripId, UUID itemId, MoveTripItineraryItemRequest request, String userId) {
        TripPermissionResponse permission = tripServiceClient.getTripPermissions(tripId, userId);

        if (permission.getRole() != TripRole.OWNER && permission.getRole() != TripRole.ADMIN) {
            throw new UnauthorizedException("Only OWNER or ADMIN can move items");
        }
        validateTripStatusForModification(permission.getStatus());

        TripItineraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!item.getTripItinerary().getTripId().equals(tripId)) {
            throw new IllegalArgumentException("Item does not belong to the specified trip");
        }

        // Logs for debugging
        System.out.println("Moving item: " + itemId + " to date: " + request.getNewDate() + ", index: "
                + request.getNewOrderIndex());

        // Apply Update
        LocalDate oldDate = item.getDate();
        LocalDate targetDate = request.getNewDate() != null ? request.getNewDate() : oldDate;
        Integer targetIndex = request.getNewOrderIndex();

        if (targetIndex != null) {
            // Re-indexing logic for the TARGET day
            // Fetch all items for the target date belonging to this itinerary
            // Note: If dateChanged is false, we are reordering in same day. item is still
            // in DB with old values.

            // We need to fetch items excluding the one we are moving (if it's already in
            // that day)
            // But simplistic approach: Fetch all, remove our item if present, insert at new
            // index, save all.
            java.util.List<TripItineraryItem> dayItems = itemRepository.findAll().stream()
                    .filter(i -> i.getTripItinerary().getId().equals(item.getTripItinerary().getId()))
                    .filter(i -> targetDate.equals(i.getDate()))
                    .filter(i -> !i.getId().equals(item.getId())) // Exclude self
                    .sorted(java.util.Comparator.comparingInt(TripItineraryItem::getOrderIndex))
                    .collect(java.util.stream.Collectors.toList());

            if (targetIndex < 0)
                targetIndex = 0;
            if (targetIndex > dayItems.size())
                targetIndex = dayItems.size();

            // Re-map the moving item
            item.setDate(targetDate);
            item.setOrderIndex(targetIndex);
            item.setUnscheduled(false);

            // Insert into list to recalc indices
            dayItems.add(targetIndex, item);

            // Update indices
            for (int i = 0; i < dayItems.size(); i++) {
                dayItems.get(i).setOrderIndex(i);
            }

            itemRepository.saveAll(dayItems);
        } else {
            // Just update date if no index provided (append to end? or keep index?)
            // Fallback to simple update
            if (request.getNewDate() != null) {
                item.setDate(request.getNewDate());
                item.setUnscheduled(false);
            }
            itemRepository.save(item);
        }
    }

    private void validateTripStatusForModification(TripStatus status) {
        if (status == TripStatus.COMPLETED || status == TripStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot modify itinerary of a COMPLETED or CANCELLED trip");
        }
        // Implicitly allows ONGOING, CONFIRMED, PLANNING, DRAFT
    }
}
