package com.triptribe.itineraryservice.ai.orchestrator;

import com.triptribe.itineraryservice.ai.client.AiModelClient;
import com.triptribe.itineraryservice.ai.dto.AiItineraryRequest;
import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;
import com.triptribe.itineraryservice.ai.dto.AiRefinementRequest;
import com.triptribe.itineraryservice.ai.prompt.DeveloperPromptProvider;
import com.triptribe.itineraryservice.ai.prompt.SystemPromptProvider;
import com.triptribe.itineraryservice.ai.validator.AiItineraryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiItineraryOrchestrator {

    private final AiModelClient aiClient;
    private final SystemPromptProvider systemPromptProvider;
    private final DeveloperPromptProvider developerPromptProvider;
    private final AiItineraryValidator validator;

    public AiItineraryResponse generateItinerary(AiItineraryRequest request) {
        log.info("Starting AI itinerary generation for destination: {}", request.getDestination());

        String systemPrompt = systemPromptProvider.getSystemPrompt();
        String developerPrompt = developerPromptProvider.createGenerationPrompt(request);

        AiItineraryResponse response = aiClient.generateItinerary(systemPrompt, developerPrompt);

        log.info("AI response received, validating...");
        validator.validate(response);

        log.info("AI itinerary validation successful");
        return response;
    }

    public AiItineraryResponse refineItinerary(String currentItineraryJson, AiRefinementRequest request) {
        log.info("Starting AI itinerary refinement: {}", request.getRefinementType());

        String systemPrompt = systemPromptProvider.getSystemPrompt();
        String developerPrompt = developerPromptProvider.createRefinementPrompt(currentItineraryJson, request);

        AiItineraryResponse response = aiClient.generateItinerary(systemPrompt, developerPrompt);

        log.info("AI refined response received, validating...");
        validator.validate(response);

        log.info("AI refined itinerary validation successful");
        return response;
    }
}
