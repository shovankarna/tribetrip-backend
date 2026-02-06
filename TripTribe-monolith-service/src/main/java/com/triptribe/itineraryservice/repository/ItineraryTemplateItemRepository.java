package com.triptribe.itineraryservice.repository;

import com.triptribe.itineraryservice.entity.ItineraryTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItineraryTemplateItemRepository extends JpaRepository<ItineraryTemplateItem, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT MAX(i.orderIndex) FROM ItineraryTemplateItem i WHERE i.template.id = :templateId")
    Integer findMaxOrderIndex(@org.springframework.data.repository.query.Param("templateId") UUID templateId);
}
