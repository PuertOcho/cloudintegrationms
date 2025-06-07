package com.cloudintegrationms.server.domain.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CloudIntegration {
    private String id;
    private String userId;
    private String provider;
    private String credentials;
    private boolean active;
} 