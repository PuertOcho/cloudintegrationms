package com.cloudintegrationms.server.infrastructure.web;

import com.cloudintegrationms.server.application.port.in.CloudIntegrationUseCase;
import com.cloudintegrationms.server.domain.model.CloudIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cloud")
@RequiredArgsConstructor
public class CloudIntegrationController {

    private final CloudIntegrationUseCase cloudIntegrationUseCase;

    @PostMapping
    public ResponseEntity<CloudIntegration> createIntegration(@RequestBody CloudIntegration integration) {
        return ResponseEntity.ok(cloudIntegrationUseCase.createIntegration(integration));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CloudIntegration> getIntegration(@PathVariable String id) {
        CloudIntegration integration = cloudIntegrationUseCase.getIntegration(id);
        return integration != null ? ResponseEntity.ok(integration) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CloudIntegration>> getUserIntegrations(@PathVariable String userId) {
        return ResponseEntity.ok(cloudIntegrationUseCase.getUserIntegrations(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIntegration(@PathVariable String id) {
        cloudIntegrationUseCase.deleteIntegration(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CloudIntegration> updateIntegration(@PathVariable String id, @RequestBody CloudIntegration integration) {
        integration.setId(id);
        return ResponseEntity.ok(cloudIntegrationUseCase.updateIntegration(integration));
    }
} 