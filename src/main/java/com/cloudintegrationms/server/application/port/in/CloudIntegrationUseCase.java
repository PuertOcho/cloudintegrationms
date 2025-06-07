package com.cloudintegrationms.server.application.port.in;

import com.cloudintegrationms.server.domain.model.CloudIntegration;
import java.util.List;

public interface CloudIntegrationUseCase {
    CloudIntegration createIntegration(CloudIntegration integration);
    CloudIntegration getIntegration(String id);
    List<CloudIntegration> getUserIntegrations(String userId);
    void deleteIntegration(String id);
    CloudIntegration updateIntegration(CloudIntegration integration);
} 