package com.cloudintegrationms.domain.notion;

import java.io.InputStream;
import java.util.Map;

public interface NotionService {
    /**
     * Obtiene la URL de autorización OAuth para Notion
     * @param state Token de estado para prevenir ataques CSRF
     * @return URL de autorización
     */
    String getAuthorizationUrl(String state);

    /**
     * Intercambia el código de autorización por un token de acceso
     * @param code Código de autorización recibido de Notion
     * @return Mapa con tokens e información de la integración
     */
    Map<String, String> exchangeCodeForTokens(String code);
    
    /**
     * Crea una nueva página en Notion
     * @param parentId ID del espacio o página padre
     * @param title Título de la página
     * @param content Contenido de la página (en formato Notion)
     * @param accessToken Token de acceso para la API de Notion
     * @return ID de la página creada
     */
    String createPage(String parentId, String title, String content, String accessToken);
    
    /**
     * Obtiene una página de Notion
     * @param pageId ID de la página
     * @param accessToken Token de acceso para la API de Notion
     * @return Contenido de la página en formato JSON
     */
    Map<String, Object> getPage(String pageId, String accessToken);
    
    /**
     * Obtiene la lista de páginas de un espacio de trabajo
     * @param accessToken Token de acceso para la API de Notion
     * @return Lista de páginas
     */
    Map<String, Object> listPages(String accessToken);
    
    /**
     * Verifica si el token de acceso es válido
     * @param accessToken Token de acceso para la API de Notion
     * @return true si el token es válido, false en caso contrario
     */
    boolean validateToken(String accessToken);

    /**
     * Sube un archivo a Notion
     * @param inputStream Stream del archivo a subir
     * @param fileName nombre del archivo
     * @param mimeType tipo MIME del archivo
     * @return ID del archivo en Notion
     */
    String uploadFile(InputStream inputStream, String fileName, String mimeType);
    
    /**
     * Obtiene la URL de visualización de un archivo
     * @param fileId ID del archivo en Notion
     * @return URL de visualización
     */
    String getFileViewUrl(String fileId);
    
    /**
     * Elimina un archivo de Notion
     * @param fileId ID del archivo a eliminar
     */
    void deleteFile(String fileId);

    /**
     * Actualiza un archivo existente en Notion
     * @param fileId ID del archivo a actualizar
     * @param inputStream Nuevo contenido del archivo
     * @param fileName Nuevo nombre del archivo
     * @param mimeType tipo MIME del archivo
     * @return ID actualizado del archivo
     */
    String updateFile(String fileId, InputStream inputStream, String fileName, String mimeType);
} 