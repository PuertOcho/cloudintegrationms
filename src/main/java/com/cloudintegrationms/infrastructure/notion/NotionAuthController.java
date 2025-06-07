package com.cloudintegrationms.infrastructure.notion;

import com.cloudintegrationms.domain.notion.NotionService;
import com.cloudintegrationms.server.domain.model.CloudIntegration;
import com.cloudintegrationms.server.application.port.in.CloudIntegrationUseCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/cloud/notion")
@Slf4j
@ConditionalOnProperty(name = "cloud-integration.providers.notion.enabled", havingValue = "true")
public class NotionAuthController {
    
    @Value("${notion.auth.success.redirect}")
    private String authSuccessRedirect;
    
    @Value("${notion.auth.failure.redirect}")
    private String authFailureRedirect;
    
    @Autowired
    private NotionService notionService;
    
    @Autowired
    private CloudIntegrationUseCase cloudIntegrationUseCase;
    
    /**
     * Inicia el flujo de autorización de Notion
     */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(HttpSession session, @RequestParam String userId) {
        try {
            // Guardar el userId en la sesión para recuperarlo después del callback
            session.setAttribute("userId", userId);
            
            // Estado aleatorio para seguridad contra CSRF
            String state = UUID.randomUUID().toString();
            session.setAttribute("oauth_state", state);
            
            // Crear URL de autorización de Notion usando el servicio
            String authUrl = notionService.getAuthorizationUrl(state);
            
            return ResponseEntity.ok(Map.of("authUrl", authUrl));
        } catch (Exception e) {
            log.error("Error generating Notion authorization URL", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error al generar URL de autorización de Notion"));
        }
    }
    
    /**
     * Maneja la redirección de Notion después de la autorización
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(@RequestParam(required = false) String code, 
                                       @RequestParam(required = false) String error,
                                       @RequestParam(required = false) String state,
                                       HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String storedState = (String) session.getAttribute("oauth_state");
        
        // Verificar estado para prevenir ataques CSRF
        if (userId == null || error != null || state == null || !state.equals(storedState)) {
            log.error("Error en callback OAuth de Notion: {}, userId: {}, state mismatch: {}", 
                     error, userId, (state != null && storedState != null));
            return new RedirectView(authFailureRedirect);
        }
        
        try {
            // Intercambiar el código por token de acceso
            Map<String, String> tokens = notionService.exchangeCodeForTokens(code);
            String accessToken = tokens.get("access_token");
            String workspaceId = tokens.get("workspace_id");
            String workspaceName = tokens.get("workspace_name");
            
            // Guardar la integración en la base de datos
            CloudIntegration integration = CloudIntegration.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .provider("notion")
                .credentials(accessToken)
                .active(true)
                .build();
            
            cloudIntegrationUseCase.createIntegration(integration);
            
            log.info("Usuario {} autenticado exitosamente con Notion. Workspace: {}", userId, workspaceName);
            
            // Construir URL de redirección con parámetros para notificar al frontend
            String successRedirectUrl = authSuccessRedirect;
            if (workspaceName != null) {
                successRedirectUrl += "&workspace=" + workspaceName;
            }
            
            return new RedirectView(successRedirectUrl);
        } catch (Exception e) {
            log.error("Error procesando callback de OAuth de Notion", e);
            return new RedirectView(authFailureRedirect);
        }
    }
    
    /**
     * Endpoint para desconectar la integración con Notion
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnectNotion(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId es requerido"));
        }
        
        try {
            // Buscar integraciones activas de Notion para este usuario
            cloudIntegrationUseCase.getUserIntegrations(userId).stream()
                .filter(integration -> "notion".equals(integration.getProvider()) && integration.isActive())
                .forEach(integration -> {
                    // Desactivar la integración
                    integration.setActive(false);
                    cloudIntegrationUseCase.updateIntegration(integration);
                });
            
            return ResponseEntity.ok(Map.of("message", "Integración con Notion desconectada exitosamente"));
        } catch (Exception e) {
            log.error("Error al desconectar integración con Notion para el usuario {}", userId, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error al desconectar: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint para verificar si un usuario está autenticado con Notion
     */
    @GetMapping("/check-auth")
    public ResponseEntity<Map<String, Object>> checkAuth(@RequestParam String userId) {
        try {
            // Buscar integraciones activas de Notion para este usuario
            boolean authenticated = cloudIntegrationUseCase.getUserIntegrations(userId).stream()
                .anyMatch(integration -> "notion".equals(integration.getProvider()) && integration.isActive());
            
            return ResponseEntity.ok(Map.of(
                "authenticated", authenticated,
                "provider", "notion"
            ));
        } catch (Exception e) {
            log.error("Error al verificar autenticación de Notion para el usuario {}", userId, e);
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
}