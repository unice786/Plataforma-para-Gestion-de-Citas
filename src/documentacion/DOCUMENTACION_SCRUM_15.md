# SCRUM-15: Recuperar Contraseña (con Mailtrap)

Documentación del trabajo realizado en la historia de usuario **Recuperar contraseña** de la plataforma Sugoi (gestión de citas).

---

## 1. Historia de Usuario y Criterios de Aceptación

**Como** cliente,  
**quiero** recuperar mi contraseña cuando la olvido,  
**para** volver a acceder a la plataforma.

### Criterios de aceptación

-  **Solicitud:** el cliente ingresa su correo en `/recuperar` y se genera un token de recuperación.
-  **Correo con enlace:** se envía un correo HTML con el enlace `/recuperar?token=...` usando **Mailtrap** (SMTP sandbox de pruebas).
-  **Validez del token:** el enlace expira a los **30 minutos** (`tokenRecuperacionExpiracion`).
-  **Token de un solo uso:** al restablecer, el token se consume (se limpia del usuario).
-  **Nueva contraseña:** validada (mínimo 8 caracteres, coincidencia con confirmación) y encriptada con **BCrypt**.
-  **Anti-enumeración:** el mensaje es genérico (*"Si el correo está registrado, recibirás las instrucciones..."*) para no revelar qué correos existen.
-  **Control de errores:** token inválido o expirado redirige a `/recuperar?error=invalid` o `/recuperar?error=expired` con mensaje claro.
-  **Confirmación:** tras restablecer, redirige a `/login?recuperada` (*"¡Contraseña actualizada!"*).

---

## 2. Archivos Creados / Modificados

Estructura por capas. Ruta base: `src/main/java/com/gestioncitas/plataformacitas/`. Base tomada de **SCRUM-12** (registro, auth y diseño ya integrados).

### DTOs (`dto/`)

| Archivo | Acción |
|---|---|
| `RestablecerPasswordDTO.java` | **Nuevo.** Nueva contraseña + confirmación + `token` oculto, con `jakarta.validation` (`@NotBlank`, `@Size`) |

### Modelos (`modelos/`)

| Archivo | Acción |
|---|---|
| `Usuario.java` | Se agregan `tokenRecuperacion` y `tokenRecuperacionExpiracion` (columnas `token_recuperacion`, `token_recuperacion_expiracion`) |

### Repositorios (`repositorios/`)

| Archivo | Acción |
|---|---|
| `UsuarioRepository.java` | Se agrega `findByTokenRecuperacion(String token)` |

### Servicios (`servicios/`)

| Archivo | Acción |
|---|---|
| `UsuarioService.java` | Nuevos métodos: `solicitarRecuperacion`, `tokenRecuperacionExiste`, `tokenRecuperacionValido`, `restablecerPassword` |
| `CorreoService.java` | Se agrega `enviarRecuperacion` (correo HTML con enlace de restablecimiento) |

### Controladores (`controladores/`)

| Archivo | Acción |
|---|---|
| `AuthController.java` | Nuevas rutas: `GET/POST /recuperar` y `POST /recuperar/restablecer` |

### Recursos y configuración

| Archivo | Acción |
|---|---|
| `src/main/resources/templates/recuperar.html` | **Nuevo.** Vista con formulario de correo y formulario de nueva contraseña (misma vista, dos estados) |
| `application.properties` | Configuración SMTP de **Mailtrap** (`sandbox.smtp.mailtrap.io:2525`, usuario/credenciales del sandbox), `app.correo.habilitado=true`, `app.url=http://localhost:8080` |
| `src/test/java/com/gestioncitas/plataformacitas/UsuarioServiceTest.java` | Pruebas de generación de token, restablecimiento válido y expirado |

---

## 3. Tabla de Rutas (Endpoints)

Base URL local: `http://localhost:8080`

| Método HTTP | Ruta URL | Descripción | Resultado |
|---|---|---|---|
| `GET` | `/recuperar` | Muestra el formulario para ingresar el correo | Vista `recuperar` |
| `POST` | `/recuperar` | Genera el token (30 min) y envía el correo con el enlace | Vista `recuperar` con mensaje genérico |
| `GET` | `/recuperar?token=...` | Valida el token y muestra el formulario de nueva contraseña | Vista `recuperar` o `?error=invalid` / `?error=expired` |
| `POST` | `/recuperar/restablecer` | Guarda la nueva contraseña encriptada y consume el token | Redirige a `/login?recuperada` o a `?error=...` |

---

## 4. Ejemplos de Formularios (POST)

### POST `/recuperar` — Solicitar enlace

| Campo | Descripción |
|---|---|
| `correo` | Correo de la cuenta (se normaliza a minúsculas). Si no existe, no se envía nada pero el mensaje es el mismo |

### POST `/recuperar/restablecer` — Nueva contraseña

| Campo | Validación |
|---|---|
| `password` | Obligatorio, mínimo 8 caracteres |
| `confirmarPassword` | Obligatorio, debe coincidir con `password` |
| `token` | Campo oculto del enlace; si es inválido o expiró → `?error=invalid` / `?error=expired` |

---

## 5. Notas Técnicas

- **Flujo completo:** 1) el cliente pide el enlace con su correo → 2) `solicitarRecuperacion` genera `UUID.randomUUID()` y fija `tokenRecuperacionExpiracion = now + 30 min` → 3) `CorreoService.enviarRecuperacion` arma el enlace `app.url + "/recuperar?token=" + token` (URL-encoded) → 4) al abrirlo, `tokenRecuperacionValido` valida existencia y expiración → 5) `restablecerPassword` encripta con BCrypt, limpia el token y guarda.
- **Mailtrap:** los correos solo son visibles en el panel de Mailtrap (https://mailtrap.io → *My Inbox*); no llegan a un correo real. Credenciales ya configuradas en `application.properties`. Con `app.correo.habilitado=false` el enlace se imprime en el log en lugar de enviarse.
- **Seguridad:** la nueva contraseña nunca se guarda en texto plano (`BCryptPasswordEncoder`). El mensaje de solicitud es genérico para evitar enumeración de cuentas. Tokens de un solo uso: tras restablecer se limpian `tokenRecuperacion` y `tokenRecuperacionExpiracion`.
- **Reutilización:** `GET /recuperar` sin token muestra el formulario de correo; con token válido muestra el de nueva contraseña (una sola vista `recuperar.html` con dos estados).
- **Pruebas:** `./mvnw test` cubre generación de token de recuperación, restablecimiento de contraseña con token válido y rechazo con token expirado.
- **Producción:** reemplazar las credenciales de Mailtrap por un SMTP real y considerar limitar reintentos de solicitud (anti-spam).