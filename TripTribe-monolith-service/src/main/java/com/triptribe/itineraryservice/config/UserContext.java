package com.triptribe.itineraryservice.config;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.Data;

@Component
@RequestScope
@Data
public class UserContext {
    private String userId;
    private String email;
    private String roles;
}
