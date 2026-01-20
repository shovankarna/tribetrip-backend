package com.triptribe.itineraryservice.ai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptribe.itineraryservice.ai.dto.AiItineraryRequest;
import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;
import com.triptribe.itineraryservice.ai.dto.AiRefinementRequest;
import com.triptribe.itineraryservice.ai.mapper.AiToTemplateMapper;
import com.triptribe.itineraryservice.ai.orchestrator.AiItineraryOrchestrator;
import com.triptribe.itineraryservice.config.UserContext;
import com.triptribe.itineraryservice.entity.ItineraryTemplate;
import com.triptribe.itineraryservice.repository.ItineraryTemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/itinerary/ai")
@RequiredArgsConstructor
@Slf4j
public class AiItineraryController {

    private final AiItineraryOrchestrator orchestrator;
    private final AiToTemplateMapper mapper;
    private final ItineraryTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    private final UserContext userContext;

    @PostMapping("/generate")
    public AiItineraryResponse generateItinerary(@Valid @RequestBody AiItineraryRequest request) {
        return orchestrator.generateItinerary(request);
    }

    @PostMapping("/refine")
    public AiItineraryResponse refineItinerary(
            @RequestBody String currentItineraryJson,
            @Valid @RequestBody AiRefinementRequest request) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "Refinement endpoint requires composite DTO definition. Use /refine-context instead.");
    }

    @PostMapping("/refine-context")
    public AiItineraryResponse refineWithContext(@RequestBody RefinementContextRequest request) {
        return orchestrator.refineItinerary(request.getCurrentItineraryJson(), request.getRefinementRequest());
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public void confirmItinerary(@RequestBody AiItineraryResponse aiResponse) {
        log.info("Confirming AI itinerary for user: {}", userContext.getUserId());

        ItineraryTemplate template = mapper.mapToTemplate(aiResponse);
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid AI response provided");
        }

        // Enrich template with ownership info
        template.setOwnerUserId(userContext.getUserId());

        templateRepository.save(template);
    }

    // Internal DTO for Refinement Context
    @lombok.Data
    public static class RefinementContextRequest {
        private String currentItineraryJson;
        @Valid
        private AiRefinementRequest refinementRequest;
    }
}
