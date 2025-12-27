package com.triptribe.itineraryservice.repository;

import com.triptribe.itineraryservice.entity.ItineraryTemplate;
import com.triptribe.itineraryservice.entity.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ItineraryTemplateRepository extends JpaRepository<ItineraryTemplate, UUID> {
    List<ItineraryTemplate> findByOwnerUserId(String ownerUserId);

    List<ItineraryTemplate> findByStatus(TemplateStatus status);
}
