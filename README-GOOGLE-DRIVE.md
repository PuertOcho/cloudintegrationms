# Integración con Google Drive para Usuarios Externos

Esta funcionalidad permite que los usuarios externos inicien sesión con su cuenta de Google y autoricen a la aplicación para subir archivos a su Google Drive.

## Configuración

### 1. Crear proyecto en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API de Google Drive para tu proyecto:
   - Ve a "APIs y Servicios" > "Biblioteca"
   - Busca "Google Drive API" y habilítala

### 2. Configurar la pantalla de consentimiento OAuth

1. Ve a "APIs y Servicios" > "Pantalla de consentimiento de OAuth"
2. Selecciona el tipo de usuario (Externo)
3. Completa la información de la aplicación:
   - Nombre de la aplicación
   - Correo electrónico de soporte
   - Logotipo (opcional)
4. En "Alcances", agrega el alcance: `https://www.googleapis.com/auth/drive.file`
5. Agrega usuarios de prueba si estás en modo prueba

### 3. Crear credenciales OAuth

1. Ve a "APIs y Servicios" > "Credenciales"
2. Haz clic en "Crear credenciales" > "ID de cliente de OAuth"
3. Selecciona "Aplicación web"
4. Agrega "URIs de redirección autorizados":
   - `http://localhost:8081/api/cloud/google-drive/callback` (para desarrollo)
   - `https://tu-dominio.com/api/cloud/google-drive/callback` (para producción)
5. Haz clic en "Crear"
6. Guarda el ID de cliente y el secreto de cliente

### 4. Configurar la aplicación

Actualiza los siguientes valores en `application.properties`:

```properties
google.client.id=TU_ID_DE_CLIENTE
google.client.secret=TU_SECRETO_DE_CLIENTE
google.redirect.uri=http://localhost:8081/api/cloud/google-drive/callback
google.auth.success.redirect=http://localhost:3000/auth/google/success
google.auth.failure.redirect=http://localhost:3000/auth/google/failure
```

Para producción, actualiza las URLs según corresponda.

## Uso de la API

### 1. Flujo de autenticación

1. Redirige al usuario a la URL de autorización:
   ```
   GET /api/cloud/google-drive/auth?userId={userId}
   ```
   Donde `userId` es el identificador único del usuario en tu sistema.

2. El usuario iniciará sesión en Google y autorizará la aplicación
3. Google redirigirá al usuario de vuelta a tu aplicación
4. El token se almacenará en la base de datos asociado al ID de usuario

### 2. Verificar estado de autenticación

```
GET /api/cloud/google-drive/check-auth?userId={userId}
```

Respuesta:
```json
{
  "authenticated": true/false
}
```

### 3. Subir archivos

```
POST /api/cloud/google-drive/user-files
Content-Type: multipart/form-data

file: [archivo a subir]
userId: [ID del usuario]
```

Respuesta exitosa:
```json
{
  "fileId": "id_del_archivo_en_google_drive",
  "viewUrl": "url_para_ver_el_archivo"
}
```

### 4. Obtener URL de visualización

```
GET /api/cloud/google-drive/user-files/{fileId}/view-url?userId={userId}
```

Respuesta:
```json
{
  "viewUrl": "url_para_ver_el_archivo"
}
```

### 5. Eliminar archivo

```
DELETE /api/cloud/google-drive/user-files/{fileId}?userId={userId}
```

Respuesta:
```json
{
  "message": "Archivo eliminado con éxito"
}
```

## Página de prueba

Para probar la integración, accede a:
```
http://localhost:8081/google-drive-auth.html
```

Esta página HTML permite:
- Autenticarse con Google Drive
- Verificar el estado de autenticación
- Subir archivos al Drive del usuario
- Ver los archivos subidos

## Notas técnicas

1. **Alcances limitados**: Esta implementación usa el alcance `drive.file`, que solo permite acceso a los archivos que la aplicación crea, no a todo el Drive del usuario.

2. **Tokens**: El sistema almacena:
   - Access token: Para acceder a la API (expira en 1 hora)
   - Refresh token: Para obtener nuevos access tokens sin requerir la autorización del usuario nuevamente
   
3. **Seguridad**: Es importante validar que el usuario que realiza las operaciones con archivos sea el propietario de esos archivos o tenga permisos para acceder a ellos.

4. **Entorno de producción**: Antes de mover a producción:
   - Actualiza el proyecto de Google Cloud para hacerlo público
   - Completa el proceso de verificación de OAuth si esperas más de 100 usuarios
   - Actualiza las URLs de redirección en la consola de Google Cloud 