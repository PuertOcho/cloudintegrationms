package com.cloudintegrationms.infrastructure.notion;

import com.cloudintegrationms.domain.notion.NotionService;
import com.cloudintegrationms.server.application.port.in.CloudIntegrationUseCase;
import com.cloudintegrationms.server.domain.model.CloudIntegration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/cloud/notion")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud-integration.providers.notion.enabled", havingValue = "true")
public class NotionController {

    private final NotionService notionService;
    private final CloudIntegrationUseCase cloudIntegrationUseCase;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String fileId = notionService.uploadFile(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        
        String viewUrl = notionService.getFileViewUrl(fileId);
        
        return ResponseEntity.ok(Map.of(
                "fileId", fileId,
                "viewUrl", viewUrl
        ));
    }

    @GetMapping("/{fileId}/view")
    public ResponseEntity<Map<String, String>> getFileViewUrl(@PathVariable String fileId) {
        String viewUrl = notionService.getFileViewUrl(fileId);
        return ResponseEntity.ok(Map.of("viewUrl", viewUrl));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String fileId) {
        notionService.deleteFile(fileId);
        return ResponseEntity.ok(Map.of("message", "Archivo eliminado con éxito"));
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<Map<String, String>> updateFile(
            @PathVariable String fileId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        String updatedFileId = notionService.updateFile(
                fileId,
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        
        String viewUrl = notionService.getFileViewUrl(updatedFileId);
        
        return ResponseEntity.ok(Map.of(
                "fileId", updatedFileId,
                "viewUrl", viewUrl
        ));
    }

    /**
     * Crea una nueva página en Notion
     */
    @PostMapping("/pages")
    public ResponseEntity<Map<String, String>> createPage(
            @RequestParam String userId,
            @RequestParam String parentId,
            @RequestBody Map<String, String> pageData) {
        
        String title = pageData.get("title");
        String content = pageData.get("content");
        
        if (title == null || parentId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se requieren los campos title y parentId"));
        }
        
        try {
            // Buscar el token de acceso del usuario
            Optional<String> accessToken = getAccessTokenForUser(userId);
            
            if (accessToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no conectado con Notion"));
            }
            
            // Crear la página
            String pageId = notionService.createPage(parentId, title, content, accessToken.get());
            
            return ResponseEntity.ok(Map.of(
                "pageId", pageId,
                "message", "Página creada exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error al crear página en Notion", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error al crear página: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene una página de Notion
     */
    @GetMapping("/pages/{pageId}")
    public ResponseEntity<Map<String, Object>> getPage(
            @PathVariable String pageId,
            @RequestParam String userId) {
        
        try {
            // Buscar el token de acceso del usuario
            Optional<String> accessToken = getAccessTokenForUser(userId);
            
            if (accessToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no conectado con Notion"));
            }
            
            // Obtener la página
            Map<String, Object> page = notionService.getPage(pageId, accessToken.get());
            
            return ResponseEntity.ok(page);
        } catch (Exception e) {
            log.error("Error al obtener página de Notion", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener página: " + e.getMessage()));
        }
    }
    
    /**
     * Lista las páginas de un usuario en Notion
     */
    @GetMapping("/pages")
    public ResponseEntity<Map<String, Object>> listPages(@RequestParam String userId) {
        try {
            // Buscar el token de acceso del usuario
            Optional<String> accessToken = getAccessTokenForUser(userId);
            
            if (accessToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no conectado con Notion"));
            }
            
            // Listar páginas
            Map<String, Object> pages = notionService.listPages(accessToken.get());
            
            return ResponseEntity.ok(pages);
        } catch (Exception e) {
            log.error("Error al listar páginas de Notion", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error al listar páginas: " + e.getMessage()));
        }
    }
    
    /**
     * Obtiene el estado de conexión con Notion para un usuario
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getConnectionStatus(@RequestParam String userId) {
        try {
            // Buscar el token de acceso del usuario
            Optional<String> accessToken = getAccessTokenForUser(userId);
            
            if (accessToken.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "connected", false
                ));
            }
            
            // Verificar si el token es válido
            boolean isValid = notionService.validateToken(accessToken.get());
            
            Map<String, Object> response = new HashMap<>();
            response.put("connected", isValid);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener estado de conexión con Notion", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error al verificar conexión: " + e.getMessage()));
        }
    }
    
    /**
     * Método auxiliar para obtener el token de acceso de Notion para un usuario
     */
    private Optional<String> getAccessTokenForUser(String userId) {
        List<CloudIntegration> integrations = cloudIntegrationUseCase.getUserIntegrations(userId);
        
        return integrations.stream()
            .filter(integration -> "notion".equals(integration.getProvider()) && integration.isActive())
            .map(CloudIntegration::getCredentials)
            .findFirst();
    }
} 