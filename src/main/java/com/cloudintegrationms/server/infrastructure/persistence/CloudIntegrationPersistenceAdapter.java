package com.cloudintegrationms.server.infrastructure.persistence;

import com.cloudintegrationms.server.application.port.in.CloudIntegrationUseCase;
import com.cloudintegrationms.server.domain.model.CloudIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CloudIntegrationPersistenceAdapter implements CloudIntegrationUseCase {
    
    private final CloudIntegrationRepository repository;

    @Override
    public CloudIntegration createIntegration(CloudIntegration integration) {
        CloudIntegrationEntity entity = toEntity(integration);
        return toDomain(repository.save(entity));
    }

    @Override
    public CloudIntegration getIntegration(String id) {
        return repository.findById(id)
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public List<CloudIntegration> getUserIntegrations(String userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteIntegration(String id) {
        repository.deleteById(id);
    }

    @Override
    public CloudIntegration updateIntegration(CloudIntegration integration) {
        CloudIntegrationEntity entity = toEntity(integration);
        return toDomain(repository.save(entity));
    }

    private CloudIntegrationEntity toEntity(CloudIntegration domain) {
        CloudIntegrationEntity entity = new CloudIntegrationEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setProvider(domain.getProvider());
        entity.setCredentials(domain.getCredentials());
        entity.setActive(domain.isActive());
        return entity;
    }

    private CloudIntegration toDomain(CloudIntegrationEntity entity) {
        return CloudIntegration.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .provider(entity.getProvider())
                .credentials(entity.getCredentials())
                .active(entity.isActive())
                .build();
    }
} 