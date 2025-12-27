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

    // --- Items ---

    @PostMapping("/{templateId}/items")
    public ItineraryTemplateItemResponse addItem(
            @PathVariable UUID templateId,
            @RequestBody CreateTemplateItemRequest request) {
        return templateService.addItem(templateId, request, userContext.getUserId());
    }
}
