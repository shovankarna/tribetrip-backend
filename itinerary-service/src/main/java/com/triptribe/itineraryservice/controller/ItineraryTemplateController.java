package com.triptribe.itineraryservice.controller;

import com.triptribe.itineraryservice.config.UserContext;
import com.triptribe.itineraryservice.dto.template.*;
import com.triptribe.itineraryservice.service.ItineraryTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries") // Template API
@RequiredArgsConstructor
public class ItineraryTemplateController {

    private final ItineraryTemplateService templateService;
    private final UserContext userContext;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItineraryTemplateResponse createTemplate(@RequestBody CreateTemplateRequest request) {
        return templateService.createTemplate(request, userContext.getUserId());
    }

    @GetMapping
    public List<ItineraryTemplateResponse> getUserTemplates() {
        return templateService.getUserTemplates(userContext.getUserId());
    }

    @GetMapping("/{templateId}")
    public ItineraryTemplateResponse getTemplate(@PathVariable UUID templateId) {
        return templateService.getTemplate(templateId);
    }

    @PutMapping("/{templateId}")
    public ItineraryTemplateResponse updateTemplate(
            @PathVariable UUID templateId,
            @RequestBody CreateTemplateRequest request) { // Reusing create request for update basics
        return templateService.updateTemplate(templateId, request, userContext.getUserId());
    }

    @PostMapping("/{templateId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveTemplate(@PathVariable UUID templateId) {
        templateService.archiveTemplate(templateId, userContext.getUserId());
    }

    @PostMapping("/{templateId}/publish")
    @ResponseStatus(HttpStatus.OK)
    public ItineraryTemplateResponse publishTemplate(@PathVariable UUID templateId) {
        return templateService.publishTemplate(templateId, userContext.getUserId());
    }

    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable UUID templateId) {
        templateService.deleteTemplate(templateId, userContext.getUserId());
    }

    // --- Items ---

    @PostMapping("/{templateId}/items")
    public ItineraryTemplateItemResponse addItem(
            @PathVariable UUID templateId,
            @RequestBody CreateTemplateItemRequest request) {
        return templateService.addItem(templateId, request, userContext.getUserId());
    }

    @PutMapping("/{templateId}/items/{itemId}")
    public ItineraryTemplateItemResponse updateItem(
            @PathVariable UUID templateId,
            @PathVariable UUID itemId,
            @RequestBody UpdateTemplateItemRequest request) {
        return templateService.updateItem(itemId, request, userContext.getUserId());
    }

    @DeleteMapping("/{templateId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @PathVariable UUID templateId,
            @PathVariable UUID itemId) {
        templateService.deleteItem(itemId, userContext.getUserId());
    }
}
