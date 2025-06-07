package com.cloudintegrationms.server.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cloud_integrations")
@Data
public class CloudIntegrationEntity {
    @Id
    private String id;
    private String userId;
    private String provider;
    private String credentials;
    private boolean active;
} 