package com.cloudintegrationms.infrastructure.notion;

import com.cloudintegrationms.domain.notion.NotionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "cloud-integration.providers.notion.enabled", havingValue = "false", matchIfMissing = true)
public class NotionMockServiceImpl implements NotionService {

    public NotionMockServiceImpl() {
        log.info("Notion integration is disabled. Using mock service.");
    }

    @Override
    public String getAuthorizationUrl(String state) {
        log.info("MOCK: Generating authorization URL with state: {}", state);
        return "https://api.notion.com/v1/oauth/authorize?mock=true&state=" + state;
    }

    @Override
    public Map<String, String> exchangeCodeForTokens(String code) {
        log.info("MOCK: Exchanging code for tokens: {}", code);
        
        // Simular una respuesta exitosa con datos de ejemplo
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", "mock_token_" + UUID.randomUUID().toString().replace("-", ""));
        tokens.put("workspace_id", "mock_workspace_" + UUID.randomUUID().toString().substring(0, 8));
        tokens.put("workspace_name", "Mi Workspace de Notion (Mock)");
        tokens.put("bot_id", "mock_bot_" + UUID.randomUUID().toString().substring(0, 8));
        
        return tokens;
    }
    
    @Override
    public String createPage(String parentId, String title, String content, String accessToken) {
        log.info("MOCK: Creating page '{}' under parent '{}' with token: {}", title, parentId, accessToken);
        return "mock_page_" + UUID.randomUUID().toString();
    }
    
    @Override
    public Map<String, Object> getPage(String pageId, String accessToken) {
        log.info("MOCK: Getting page with ID '{}' using token: {}", pageId, accessToken);
        
        Map<String, Object> page = new HashMap<>();
        page.put("id", pageId);
        page.put("url", "https://notion.so/mock/" + pageId);
        page.put("created_time", "2023-01-01T00:00:00.000Z");
        page.put("last_edited_time", "2023-01-01T00:00:00.000Z");
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> title = new HashMap<>();
        title.put("title", "Página Mock de Notion");
        properties.put("title", title);
        page.put("properties", properties);
        
        return page;
    }
    
    @Override
    public Map<String, Object> listPages(String accessToken) {
        log.info("MOCK: Listing pages with token: {}", accessToken);
        
        Map<String, Object> result = new HashMap<>();
        result.put("object", "list");
        result.put("has_more", false);
        
        // Crear algunas páginas de ejemplo
        Object[] pages = new Object[3];
        for (int i = 0; i < 3; i++) {
            String pageId = "mock_page_" + i;
            Map<String, Object> page = new HashMap<>();
            page.put("id", pageId);
            page.put("url", "https://notion.so/mock/" + pageId);
            page.put("created_time", "2023-01-01T00:00:00.000Z");
            
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> title = new HashMap<>();
            title.put("title", "Página Mock " + (i + 1));
            properties.put("title", title);
            page.put("properties", properties);
            
            pages[i] = page;
        }
        
        result.put("results", pages);
        
        return result;
    }
    
    @Override
    public boolean validateToken(String accessToken) {
        log.info("MOCK: Validating token: {}", accessToken);
        // Simulamos que el token es válido si comienza con "mock_token_"
        return accessToken != null && accessToken.startsWith("mock_token_");
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String mimeType) {
        log.info("MOCK: Uploading file to Notion: {}, type: {}", fileName, mimeType);
        // Generar un ID aleatorio para simular la respuesta
        return "mock_notion_file_" + UUID.randomUUID().toString();
    }

    @Override
    public String getFileViewUrl(String fileId) {
        log.info("MOCK: Getting file view URL for ID: {}", fileId);
        // Simular una URL de visualización
        return "https://notion.so/mock/" + fileId;
    }

    @Override
    public void deleteFile(String fileId) {
        log.info("MOCK: Deleting file with ID: {}", fileId);
        // No hace nada realmente, solo registra la llamada
    }

    @Override
    public String updateFile(String fileId, InputStream inputStream, String fileName, String mimeType) {
        log.info("MOCK: Updating file: {}, ID: {}, type: {}", fileName, fileId, mimeType);
        // Devolver el mismo ID para simular que se actualizó correctamente
        return fileId;
    }
} 