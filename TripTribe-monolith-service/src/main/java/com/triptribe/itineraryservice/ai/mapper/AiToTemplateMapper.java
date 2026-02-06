package com.triptribe.itineraryservice.ai.mapper;

import com.triptribe.itineraryservice.ai.dto.AiActivityItem;
import com.triptribe.itineraryservice.ai.dto.AiDayPlan;
import com.triptribe.itineraryservice.ai.dto.AiItineraryResponse;
import com.triptribe.itineraryservice.entity.ItineraryTemplate;
import com.triptribe.itineraryservice.entity.ItineraryTemplateItem;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiToTemplateMapper {

    public ItineraryTemplate mapToTemplate(AiItineraryResponse aiResponse) {
        if (aiResponse == null) {
            return null;
        }

        ItineraryTemplate template = new ItineraryTemplate();
        template.setTitle(aiResponse.getTitle());
        template.setDescription(aiResponse.getDescription());
        // template.setTags() - not available in current entity
        // default status to DRAFT

        List<ItineraryTemplateItem> items = new ArrayList<>();

        if (aiResponse.getDays() != null) {
            for (AiDayPlan day : aiResponse.getDays()) {
                if (day.getItems() != null) {
                    for (AiActivityItem aiItem : day.getItems()) {
                        ItineraryTemplateItem item = mapItem(aiItem, day.getDayOffset(), template);
                        items.add(item);
                    }
                }
            }
        }

        template.setItems(items);
        return template;
    }

    private ItineraryTemplateItem mapItem(AiActivityItem aiItem, int dayOffset, ItineraryTemplate template) {
        ItineraryTemplateItem item = new ItineraryTemplateItem();
        item.setTemplate(template);
        item.setDefaultDayOffset(dayOffset);
        item.setTitle(aiItem.getTitle());
        item.setDescription(aiItem.getDescription());
        item.setLocationText(aiItem.getLocation());
        item.setNotes(aiItem.getNotes());
        item.setOrderIndex(aiItem.getOrder());
        item.setDefaultDurationMinutes(aiItem.getDurationMinutes());

        if (aiItem.getStartTime() != null) {
            try {
                item.setDefaultStartTime(LocalTime.parse(aiItem.getStartTime()));
            } catch (Exception e) {
                // ignore or log invalid time format
            }
        }

        return item;
    }
}
