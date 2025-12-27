package com.triptribe.itineraryservice.dto.template;

import com.triptribe.itineraryservice.entity.TemplateStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ItineraryTemplateResponse {
    private UUID id;
    private String title;
    private String description;
    private String ownerUserId;
    private TemplateStatus status;
    private List<ItineraryTemplateItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
