package com.triptribe.itineraryservice.service;

import com.triptribe.itineraryservice.dto.template.*;

import java.util.List;
import java.util.UUID;

public interface ItineraryTemplateService {
    ItineraryTemplateResponse createTemplate(CreateTemplateRequest request, String userId);

    List<ItineraryTemplateResponse> getUserTemplates(String userId);

    ItineraryTemplateResponse getTemplate(UUID templateId);

    ItineraryTemplateResponse updateTemplate(UUID templateId, CreateTemplateRequest request, String userId);

    ItineraryTemplateItemResponse addItem(UUID templateId, CreateTemplateItemRequest request, String userId);

    ItineraryTemplateItemResponse updateItem(UUID itemId, UpdateTemplateItemRequest request, String userId);

    void deleteItem(UUID itemId, String userId);

    void archiveTemplate(UUID templateId, String userId);

    // Additional methods for attach readiness?
    void validateTemplateForAttach(UUID templateId);
}
