package com.triptribe.itineraryservice.mapper;

import com.triptribe.itineraryservice.dto.itinerary.*;
import com.triptribe.itineraryservice.dto.template.*;
import com.triptribe.itineraryservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItineraryMapper {

    // --- Template Mappings ---

    public ItineraryTemplateResponse toDto(ItineraryTemplate template) {
        ItineraryTemplateResponse dto = new ItineraryTemplateResponse();
        dto.setId(template.getId());
        dto.setTitle(template.getTitle());
        dto.setDescription(template.getDescription());
        dto.setOwnerUserId(template.getOwnerUserId());
        dto.setStatus(template.getStatus());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());

        if (template.getItems() != null) {
            dto.setItems(template.getItems().stream()
                    .map(this::toDto)
                    .sorted(Comparator.comparing(ItineraryTemplateItemResponse::getOrderIndex))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public ItineraryTemplateItemResponse toDto(ItineraryTemplateItem item) {
        ItineraryTemplateItemResponse dto = new ItineraryTemplateItemResponse();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setDefaultDayOffset(item.getDefaultDayOffset());
        dto.setDefaultStartTime(item.getDefaultStartTime());
        dto.setDefaultDurationMinutes(item.getDefaultDurationMinutes());
        dto.setLocationText(item.getLocationText());
        dto.setNotes(item.getNotes());
        dto.setOrderIndex(item.getOrderIndex());
        return dto;
    }

    public ItineraryTemplate toEntity(CreateTemplateRequest request, String userId) {
        ItineraryTemplate template = new ItineraryTemplate();
        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());
        template.setOwnerUserId(userId);
        template.setStatus(TemplateStatus.DRAFT);
        return template;
    }

    public ItineraryTemplateItem toEntity(CreateTemplateItemRequest request, ItineraryTemplate template) {
        ItineraryTemplateItem item = new ItineraryTemplateItem();
        item.setTemplate(template);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setDefaultDayOffset(request.getDefaultDayOffset());
        item.setDefaultStartTime(request.getDefaultStartTime());
        item.setDefaultDurationMinutes(request.getDefaultDurationMinutes());
        item.setLocationText(request.getLocationText());
        item.setNotes(request.getNotes());
        item.setOrderIndex(request.getOrderIndex());
        return item;
    }

    // --- Trip Itinerary Mappings ---

    public TripItineraryResponse toDto(TripItinerary itinerary) {
        TripItineraryResponse dto = new TripItineraryResponse();
        dto.setId(itinerary.getId());
        dto.setTripId(itinerary.getTripId());
        dto.setTemplateId(itinerary.getTemplateId());
        dto.setCreatedByUserId(itinerary.getCreatedByUserId());
        dto.setCreatedAt(itinerary.getCreatedAt());

        if (itinerary.getItems() != null) {
            dto.setItems(itinerary.getItems().stream()
                    .map(this::toDto)
                    .sorted(Comparator
                            .comparing(TripItineraryItemResponse::getDate,
                                    Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(TripItineraryItemResponse::getStartTime,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public TripItineraryItemResponse toDto(TripItineraryItem item) {
        TripItineraryItemResponse dto = new TripItineraryItemResponse();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDate(item.getDate());
        dto.setStartTime(item.getStartTime());
        dto.setDurationMinutes(item.getDurationMinutes());
        dto.setLocationText(item.getLocationText());
        dto.setNotes(item.getNotes());
        dto.setOrderIndex(item.getOrderIndex());
        dto.setUnscheduled(item.isUnscheduled());
        dto.setCompleted(item.isCompleted());
        return dto;
    }

    public TripItineraryItem toTripItem(ItineraryTemplateItem templateItem, java.time.LocalDate date) {
        TripItineraryItem item = new TripItineraryItem();
        item.setTitle(templateItem.getTitle());
        item.setDate(date);
        item.setStartTime(templateItem.getDefaultStartTime());
        item.setDurationMinutes(templateItem.getDefaultDurationMinutes());
        item.setLocationText(templateItem.getLocationText());
        item.setNotes(templateItem.getNotes());
        item.setOrderIndex(templateItem.getOrderIndex());
        item.setUnscheduled(date == null); // Unscheduled if no date calculated
        return item;
    }

    public TripItineraryItem toEntity(CreateTripItineraryItemRequest request, TripItinerary itinerary) {
        TripItineraryItem item = new TripItineraryItem();
        item.setTripItinerary(itinerary);
        item.setTitle(request.getTitle());
        item.setDate(request.getDate());
        item.setStartTime(request.getStartTime());
        item.setDurationMinutes(request.getDurationMinutes());
        item.setLocationText(request.getLocationText());
        item.setNotes(request.getNotes());
        item.setOrderIndex(request.getOrderIndex());
        item.setUnscheduled(request.getIsUnscheduled() != null && request.getIsUnscheduled());
        return item;
    }
}
