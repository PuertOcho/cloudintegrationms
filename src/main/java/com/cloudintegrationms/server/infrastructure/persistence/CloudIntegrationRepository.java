package com.cloudintegrationms.server.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloudIntegrationRepository extends JpaRepository<CloudIntegrationEntity, String> {
    List<CloudIntegrationEntity> findByUserId(String userId);
} 