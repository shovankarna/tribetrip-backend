package com.triptribe.itineraryservice.dto.template;

import lombok.Data;

@Data
public class CreateTemplateRequest {
    private String title;
    private String description;
}
