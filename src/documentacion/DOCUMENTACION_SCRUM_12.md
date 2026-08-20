# SCRUM-12: Registrar Cliente y Login

Documentación del trabajo realizado en la historia de usuario **Registrar cliente y autenticación** de la plataforma Sugoi (gestión de citas).

---

## 1. Historia de Usuario y Criterios de Aceptación

**Como** cliente,  
**quiero** registrarme y acceder a mi cuenta,  
**para** usar la plataforma de forma segura.

### Criterios de aceptación

-  **Registro:** formulario con nombre, correo, teléfono, contraseña y confirmación, validado en backend.
-  **Contraseña segura:** se guarda encriptada con **BCrypt** (nunca en texto plano).
-  **Correo duplicado:** no se permite registrar dos cuentas con el mismo correo.
-  **Verificación de correo:** se genera un token de verificación (validez 24 h) y se envía un correo de bienvenida con el enlace (opcional, no bloquea el acceso).
-  **Auto-login:** tras el registro, la sesión se inicia y el cliente entra directo a `/inicio`.
-  **Login:** `POST /login` autentica con correo y contraseña usando `PasswordEncoder.matches()`.
-  **Sesión:** el usuario autenticado queda en `HttpSession`; `/inicio` redirige a `/login` si no hay sesión.
-  **Logout:** `GET /logout` invalida la sesión.
-  **Protección contra mass assignment:** el registro usa el DTO `ClienteRegistroDTO`, no la entidad.

---

## 2. Archivos Creados / Modificados

Estructura por capas. Ruta base: `src/main/java/com/gestioncitas/plataformacitas/`.

### Controladores (`controladores/`)

| Archivo | Acción |
|---|---|
| `AuthController.java` | **Nuevo.** Rutas de login, registro, verificación, inicio y logout |
| `RegistroController.java` | **Eliminado.** Reemplazado por `AuthController` (guardaba la entidad sin validación ni BCrypt) |
| `UsuarioController.java` | **Eliminado.** Stub sin uso |

### DTOs (`dto/`)

| Archivo | Uso |
|---|---|
| `ClienteRegistroDTO.java` | Datos del formulario de registro con `jakarta.validation` (`@NotBlank`, `@Email`, `@Size`, `@Pattern`) |

### Modelos (`modelos/`)

| Archivo | Acción |
|---|---|
| `Usuario.java` | Entidad abstracta, tabla `usuarios`, herencia `JOINED`; campos comunes: `nombre`, `correo` (único), `password`, `activo`, `verificado`, `tokenVerificacion`, `tokenExpiracion` |
| `Cliente.java` | Extiende `Usuario`, tabla `clientes`, `@PrimaryKeyJoinColumn(name = "usuario_id")`, campo propio `telefono` |

### Repositorios (`repositorios/`)

| Archivo | Acción |
|---|---|
| `UsuarioRepository.java` | `JpaRepository<Usuario, Long>` + `findByCorreo`, `findByTokenVerificacion`, `existsByCorreo` |
| `ClienteRepository.java` | `JpaRepository<Cliente, Long>` |

### Servicios (`servicios/`)

| Archivo | Acción |
|---|---|
| `UsuarioService.java` | Reglas de negocio: `registrar`, `autenticar`, `verificar` |
| `CorreoService.java` | **Nuevo.** Envío de correos HTML vía **Spring Mail + Mailtrap** |

### Configuración y recursos

| Archivo | Acción |
|---|---|
| `config/SecurityConfig.java` | Bean `BCryptPasswordEncoder`; acceso abierto a todas las rutas (la sesión se valida manualmente), CSRF activado |
| `pom.xml` | Dependencias `spring-boot-starter-validation`, `spring-boot-starter-mail`, `spring-boot-starter-security` |
| `application.properties` | MySQL `plataforma_citas` (Docker), configuración SMTP de Mailtrap, `app.url` |
| `docker-compose.yml` | Contenedor `mysql:8.0` con el esquema `plataforma_citas` |
| `src/main/resources/templates/registro.html` | Formulario Thymeleaf con validación y mensajes de error |
| `src/main/resources/templates/login.html` | Formulario de inicio de sesión |
| `src/main/resources/templates/inicio.html` | Pantalla principal con nombre del usuario |
| `src/main/resources/static/css/styles.css` | Diseño personalizado (V3) |
| `src/test/java/com/gestioncitas/plataformacitas/UsuarioServiceTest.java` | Pruebas unitarias (Mockito) del servicio |

---

## 3. Tabla de Rutas (Endpoints)

Base URL local: `http://localhost:8080`

| Método HTTP | Ruta URL | Descripción | Resultado |
|---|---|---|---|
| `GET` | `/registro` | Muestra el formulario de registro | Vista `registro` |
| `POST` | `/registro` | Crea la cuenta, inicia sesión y envía correo de verificación | Redirige a `/inicio?bienvenido` |
| `GET` | `/login` | Muestra el formulario de login | Vista `login` |
| `POST` | `/login` | Autentica con correo + contraseña (BCrypt) | Redirige a `/inicio` o error |
| `GET` | `/verificar?token=...` | Confirma la cuenta con el token (24 h de validez) | Redirige a `/login?verificado` |
| `GET` | `/inicio` | Pantalla principal (requiere sesión) | Vista `inicio` o redirige a `/login` |
| `GET` | `/logout` | Invalida la sesión | Redirige a `/login` |

---

## 4. Ejemplos de Formularios (POST)

### POST `/registro` — Crear cuenta

Campos del formulario (`application/x-www-form-urlencoded`, CSRF token incluido por Thymeleaf):

| Campo | Validación |
|---|---|
| `nombre` | Obligatorio, 3–100 caracteres |
| `correo` | Obligatorio, formato válido (`@Email`) |
| `telefono` | Obligatorio, 7–15 dígitos (`^[0-9]{7,15}$`) |
| `password` | Obligatorio, mínimo 8 caracteres |
| `confirmarPassword` | Obligatorio, debe coincidir con `password` |

Si el correo ya existe se muestra el error *"Ya existe una cuenta con ese correo"* y si las contraseñas no coinciden *"Las contraseñas no coinciden"*.

### POST `/login` — Iniciar sesión

| Campo | Descripción |
|---|---|
| `correo` | Correo registrado (se normaliza a minúsculas) |
| `password` | Contraseña, se compara contra el hash BCrypt |

Credenciales incorrectas → mensaje *"Credenciales incorrectas"* en la vista `login`.

---

## 5. Notas Técnicas

- **Base de datos:** MySQL en Docker (`mysql:8.0`), esquema `plataforma_citas`. Hibernate con `ddl-auto=update`.
- **Herencia JPA:** `Cliente` extiende `Usuario` con `InheritanceType.JOINED` y `@PrimaryKeyJoinColumn(name = "usuario_id")`, alineado al DER.
- **Seguridad:** `SecurityConfig` expone el bean `BCryptPasswordEncoder`; todas las rutas están permitidas y la protección de `/inicio` se hace validando la sesión manualmente en el controlador. CSRF activado (los formularios Thymeleaf inyectan el token automáticamente).
- **Sesión:** `HttpSession` guarda el objeto `Usuario` autenticado; `GET /logout` la invalida.
- **Tokens:** UUID aleatorios (`UUID.randomUUID()`); verificación válida 24 horas. Al verificar, el token se limpia y `verificado = true`.
- **Correos (Mailtrap):** `CorreoService` envía HTML con `JavaMailSender` al sandbox `sandbox.smtp.mailtrap.io` (puerto 2525). Con `app.correo.habilitado=false` no envía y solo registra el enlace en el log (útil en desarrollo).
- **Pruebas:** `./mvnw test` cubre registro exitoso (password encriptada + token), correo duplicado, contraseñas que no coinciden, rechazo de contraseñas en texto plano y verificación de token válido/expirado.
- **Nota de producción:** restringir el acceso por rol en `SecurityConfig` y reemplazar las credenciales de Mailtrap por un SMTP real (ej. Gmail con Contraseña de aplicación).