package com.cloudintegrationms.infrastructure.notion;

import com.cloudintegrationms.domain.notion.NotionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "cloud-integration.providers.notion.enabled", havingValue = "true")
public class NotionServiceImpl implements NotionService {

    @Value("${notion.client.id}")
    private String clientId;
    
    @Value("${notion.client.secret}")
    private String clientSecret;
    
    @Value("${notion.redirect.uri}")
    private String redirectUri;
    
    @Value("${notion.api.version}")
    private String notionApiVersion;

    private final OkHttpClient client = new OkHttpClient();
    private static final String NOTION_API_BASE_URL = "https://api.notion.com/v1";
    private static final String NOTION_AUTH_URL = "https://api.notion.com/v1/oauth/authorize";
    private static final String NOTION_TOKEN_URL = "https://api.notion.com/v1/oauth/token";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getAuthorizationUrl(String state) {
        try {
            return NOTION_AUTH_URL +
                    "?client_id=" + clientId +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString()) +
                    "&response_type=code" +
                    "&owner=user" +
                    "&state=" + state;
        } catch (Exception e) {
            log.error("Error al generar URL de autorización de Notion", e);
            throw new RuntimeException("Error al generar URL de autorización de Notion", e);
        }
    }

    @Override
    public Map<String, String> exchangeCodeForTokens(String code) {
        try {
            String credentials = okhttp3.Credentials.basic(clientId, clientSecret);
            
            RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .build();

            Request request = new Request.Builder()
                .url(NOTION_TOKEN_URL)
                .post(formBody)
                .header("Authorization", credentials)
                .header("Content-Type", "application/json")
                .header("Notion-Version", notionApiVersion)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("Error exchanging code for tokens: {}", errorBody);
                    throw new IOException("Error al intercambiar código por tokens: " + response.code());
                }
                
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                Map<String, String> result = new HashMap<>();
                result.put("access_token", (String) responseMap.get("access_token"));
                
                // Extraer información del workspace si está disponible
                if (responseMap.containsKey("workspace_id")) {
                    result.put("workspace_id", (String) responseMap.get("workspace_id"));
                }
                if (responseMap.containsKey("workspace_name")) {
                    result.put("workspace_name", (String) responseMap.get("workspace_name"));
                }
                if (responseMap.containsKey("workspace_icon")) {
                    result.put("workspace_icon", (String) responseMap.get("workspace_icon"));
                }
                if (responseMap.containsKey("bot_id")) {
                    result.put("bot_id", (String) responseMap.get("bot_id"));
                }
                
                return result;
            }
        } catch (Exception e) {
            log.error("Error exchanging code for tokens", e);
            throw new RuntimeException("Error al intercambiar código por tokens", e);
        }
    }

    @Override
    public String createPage(String parentId, String title, String content, String accessToken) {
        try {
            String jsonBody = buildCreatePageRequest(parentId, title, content);
            
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(NOTION_API_BASE_URL + "/pages")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Notion-Version", notionApiVersion)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error al crear página en Notion: " + response.code());
                }
                
                String responseBody = response.body().string();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                return (String) responseMap.get("id");
            }
        } catch (Exception e) {
            log.error("Error al crear página en Notion", e);
            throw new RuntimeException("Error al crear página en Notion", e);
        }
    }

    private String buildCreatePageRequest(String parentId, String title, String content) {
        // Este es un ejemplo simplificado de la estructura para crear una página
        // En una implementación completa, habría que construir el cuerpo de la solicitud
        // según la estructura de bloques de Notion
        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            // Configurar el parent
            Map<String, Object> parent = new HashMap<>();
            parent.put("page_id", parentId);
            requestBody.put("parent", parent);
            
            // Propiedades (título)
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> titleProp = new HashMap<>();
            Map<String, Object> titleContent = new HashMap<>();
            titleContent.put("content", title);
            titleProp.put("title", new Object[]{titleContent});
            properties.put("title", titleProp);
            requestBody.put("properties", properties);
            
            // Convertir a JSON
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Error al construir el cuerpo de la solicitud para crear página", e);
            throw new RuntimeException("Error al construir el cuerpo de la solicitud para crear página", e);
        }
    }

    @Override
    public Map<String, Object> getPage(String pageId, String accessToken) {
        try {
            Request request = new Request.Builder()
                    .url(NOTION_API_BASE_URL + "/pages/" + pageId)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Notion-Version", notionApiVersion)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error al obtener página de Notion: " + response.code());
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Map.class);
            }
        } catch (Exception e) {
            log.error("Error al obtener página de Notion", e);
            throw new RuntimeException("Error al obtener página de Notion", e);
        }
    }

    @Override
    public Map<String, Object> listPages(String accessToken) {
        try {
            // Utilizamos la API de búsqueda para listar las páginas
            String jsonBody = "{\"filter\": {\"property\": \"object\", \"value\": \"page\"}}";
            
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(NOTION_API_BASE_URL + "/search")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Notion-Version", notionApiVersion)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Error al listar páginas de Notion: " + response.code());
                }
                
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Map.class);
            }
        } catch (Exception e) {
            log.error("Error al listar páginas de Notion", e);
            throw new RuntimeException("Error al listar páginas de Notion", e);
        }
    }

    @Override
    public boolean validateToken(String accessToken) {
        try {
            Request request = new Request.Builder()
                    .url(NOTION_API_BASE_URL + "/users/me")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Notion-Version", notionApiVersion)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.error("Error al validar token de Notion", e);
            return false;
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, String mimeType) {
        // Notion no permite subir archivos directamente a través de su API
        // Se necesitaría utilizar una solución alternativa como subir a S3 y luego vincular
        log.warn("La API de Notion no soporta la subida directa de archivos");
        throw new UnsupportedOperationException("La API de Notion no soporta la subida directa de archivos");
    }

    @Override
    public String getFileViewUrl(String fileId) {
        log.warn("La API de Notion no soporta la obtención directa de URLs de archivos");
        throw new UnsupportedOperationException("La API de Notion no soporta la obtención directa de URLs de archivos");
    }

    @Override
    public void deleteFile(String fileId) {
        log.warn("La API de Notion no soporta la eliminación directa de archivos");
        throw new UnsupportedOperationException("La API de Notion no soporta la eliminación directa de archivos");
    }

    @Override
    public String updateFile(String fileId, InputStream inputStream, String fileName, String mimeType) {
        log.warn("La API de Notion no soporta la actualización directa de archivos");
        throw new UnsupportedOperationException("La API de Notion no soporta la actualización directa de archivos");
    }
} 