package com.triptribe.itineraryservice.service.impl;

import com.triptribe.itineraryservice.dto.template.*;
import com.triptribe.itineraryservice.entity.ItineraryTemplate;
import com.triptribe.itineraryservice.entity.ItineraryTemplateItem;
import com.triptribe.itineraryservice.entity.TemplateStatus;
import com.triptribe.itineraryservice.exception.ResourceNotFoundException;
import com.triptribe.itineraryservice.exception.UnauthorizedException;
import com.triptribe.itineraryservice.mapper.ItineraryMapper;
import com.triptribe.itineraryservice.repository.ItineraryTemplateItemRepository;
import com.triptribe.itineraryservice.repository.ItineraryTemplateRepository;
import com.triptribe.itineraryservice.service.ItineraryTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryTemplateServiceImpl implements ItineraryTemplateService {

    private final ItineraryTemplateRepository templateRepository;
    private final ItineraryTemplateItemRepository itemRepository;
    private final ItineraryMapper mapper;

    @Override
    @Transactional
    public ItineraryTemplateResponse createTemplate(CreateTemplateRequest request, String userId) {
        ItineraryTemplate template = mapper.toEntity(request, userId);
        return mapper.toDto(templateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryTemplateResponse> getUserTemplates(String userId) {
        return templateRepository.findByOwnerUserId(userId).stream()
                .filter(t -> t.getStatus() != TemplateStatus.ARCHIVED) // Filter archived? Or show them?
                // Requirement says owner sees unused... let's show all except truly deleted?
                // Or show archived separately. For MVP, show all logical ones.
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItineraryTemplateResponse getTemplate(UUID templateId) {
        ItineraryTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return mapper.toDto(template);
    }

    @Override
    @Transactional
    public ItineraryTemplateResponse updateTemplate(UUID templateId, CreateTemplateRequest request, String userId) {
        ItineraryTemplate template = validateOwner(templateId, userId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot update archived template");
        }

        if (request.getTitle() != null)
            template.setTitle(request.getTitle());
        if (request.getDescription() != null)
            template.setDescription(request.getDescription());

        return mapper.toDto(templateRepository.save(template));
    }

    @Override
    @Transactional
    public ItineraryTemplateItemResponse addItem(UUID templateId, CreateTemplateItemRequest request, String userId) {
        ItineraryTemplate template = validateOwner(templateId, userId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot add items to archived template");
        }

        ItineraryTemplateItem item = mapper.toEntity(request, template);

        if (item.getOrderIndex() == null) {
            Integer maxOrder = itemRepository.findMaxOrderIndex(template.getId());
            item.setOrderIndex(maxOrder != null ? maxOrder + 1 : 0);
        }

        return mapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItineraryTemplateItemResponse updateItem(UUID itemId, UpdateTemplateItemRequest request, String userId) {
        ItineraryTemplateItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        ItineraryTemplate template = item.getTemplate();
        if (!template.getOwnerUserId().equals(userId)) {
            throw new UnauthorizedException("Only owner can update template items");
        }

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot update items in archived template");
        }

        if (request.getTitle() != null)
            item.setTitle(request.getTitle());
        if (request.getDescription() != null)
            item.setDescription(request.getDescription());
        if (request.getDefaultDayOffset() != null)
            item.setDefaultDayOffset(request.getDefaultDayOffset());
        if (request.getDefaultStartTime() != null)
            item.setDefaultStartTime(request.getDefaultStartTime());
        if (request.getDefaultDurationMinutes() != null)
            item.setDefaultDurationMinutes(request.getDefaultDurationMinutes());
        if (request.getLocationText() != null)
            item.setLocationText(request.getLocationText());
        if (request.getNotes() != null)
            item.setNotes(request.getNotes());
        if (request.getOrderIndex() != null)
            item.setOrderIndex(request.getOrderIndex());

        return mapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(UUID itemId, String userId) {
        ItineraryTemplateItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        ItineraryTemplate template = item.getTemplate();
        if (!template.getOwnerUserId().equals(userId)) {
            throw new UnauthorizedException("Only owner can delete template items");
        }

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot delete items in archived template");
        }

        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public ItineraryTemplateResponse publishTemplate(UUID templateId, String userId) {
        ItineraryTemplate template = validateOwner(templateId, userId);

        if (template.getStatus() == TemplateStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot publish archived template");
        }

        template.setStatus(TemplateStatus.ACTIVE);
        return mapper.toDto(templateRepository.save(template));
    }

    @Override
    @Transactional
    public void archiveTemplate(UUID templateId, String userId) {
        ItineraryTemplate template = validateOwner(templateId, userId);
        template.setStatus(TemplateStatus.ARCHIVED);
        templateRepository.save(template);
    }

    @Override
    public void validateTemplateForAttach(UUID templateId) {
        ItineraryTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        if (template.getStatus() != TemplateStatus.ACTIVE && template.getStatus() != TemplateStatus.DRAFT) {
            // Requirement says DRAFT not attachable?
            // "DRAFT | Attachable ❌" in request description table.
            // So only ACTIVE.
        }

        // Wait, "DRAFT | Attachable ❌". "ACTIVE | Attachable ✅".
        if (template.getStatus() != TemplateStatus.ACTIVE) {
            throw new IllegalArgumentException("Template is not ACTIVE");
        }
    }

    private ItineraryTemplate validateOwner(UUID templateId, String userId) {
        ItineraryTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        if (!template.getOwnerUserId().equals(userId)) {
            throw new UnauthorizedException("Only owner can modify template");
        }
        return template;
    }
}
