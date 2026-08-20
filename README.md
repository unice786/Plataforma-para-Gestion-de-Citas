# Sugoi - Plataforma para Gestión de Citas

## Diagrama de Clases
![Diagrama de Clases](https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_clases.puml&v=3)

## Diagrama Entidad-Relación (DER)
![Diagrama DER](https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_bd.puml&v=3)

Sistema de gestión de citas desarrollado con **Spring Boot 3 + Spring Data JPA + MySQL**. El backend y las vistas de autenticación se construyen con Thymeleaf, y la seguridad de contraseñas usa **BCrypt**.

---

## Tecnologías

| Capa        | Tecnología                                |
|-------------|-------------------------------------------|
| Backend     | Java 21, Spring Boot 3.3.4                |
| Persistencia| Spring Data JPA (Hibernate), MySQL 8      |
| Seguridad   | Spring Security (BCryptPasswordEncoder)   |
| Vistas      | Thymeleaf + Bootstrap 5                   |
| Validación  | Jakarta Validation (Bean Validation)      |
| Correos     | Spring Mail + Mailtrap (SMTP sandbox)     |
| Base de datos local | Docker Compose (mysql:8.0)         |

---

## Historias de usuario

### SCRUM-12 — Registrar cliente

> **Como** cliente, **quiero** registrarme y crear una cuenta **para** acceder a la plataforma de forma segura.

| Criterio de aceptación                    | Estado |
|-------------------------------------------|--------|
| Formulario con validación de datos        | ✅ Implementado |
| Verificación de correo (opcional, no bloquea) | ✅ Implementado |
| Contraseña encriptada (BCrypt)            | ✅ Implementado |
| Mensaje de confirmación al registrarse    | ✅ Implementado (pantalla de inicio) |

#### Flujo de registro

1. El cliente llena el formulario en `/registro` con nombre, correo, teléfono, contraseña y confirmación.
2. **Validación backend** (Jakarta Validation): nombre obligatorio (3–100 caracteres), correo con formato válido, teléfono de 7–15 dígitos, contraseña de mínimo 8 caracteres y coincidencia con la confirmación. Los errores se muestran debajo de cada campo.
3. El servicio verifica que el correo no exista, encripta la contraseña con **BCrypt** y genera un **token de verificación** con validez de 24 horas.
4. Se envía un correo de bienvenida con el enlace `/verificar?token=...` (opcional, no bloquea el acceso). Los correos llegan a la bandeja de **Mailtrap**.
5. La sesión se inicia automáticamente y el cliente entra directo a `/inicio` con el mensaje *"¡Registro exitoso! Bienvenido/a..."*.

### SCRUM-12b — Recuperar contraseña

> **Como** cliente, **quiero** recuperar mi contraseña **para** volver a acceder a la plataforma.

| Criterio de aceptación                            | Estado |
|---------------------------------------------------|--------|
| Solicitud con el correo registrado                | ✅ Implementado |
| Enlace de restablecimiento por correo (30 min de validez) | ✅ Implementado |
| Nueva contraseña validada y encriptada (BCrypt)   | ✅ Implementado |
| Mensaje de confirmación al restablecer            | ✅ Implementado |

#### Flujo de recuperación

1. El cliente ingresa su correo en `/recuperar`.
2. Si el correo existe, se genera un **token de recuperación** (30 minutos de validez) y se envía el enlace `/recuperar?token=...` (el mensaje mostrado es genérico para no revelar correos existentes). Si el token es inválido o expiró, se redirige a `/recuperar?error=invalid` o `/recuperar?error=expired` con un mensaje claro.
3. Al abrir el enlace, se muestra el formulario de nueva contraseña con validación (mínimo 8 caracteres y coincidencia).
4. La contraseña se guarda encriptada con **BCrypt**, el token se consume y se redirige al login con el mensaje *"¡Contraseña actualizada!"*.

---

## Requisitos

- Java 21
- Maven (o usar el wrapper `./mvnw`)
- Docker (para la base de datos MySQL)

---

## Puesta en marcha

### 1. Levantar la base de datos

```bash
docker compose up -d
```

Crea el contenedor `mysql_citas` con la base `plataforma_citas` (usuario `root` / clave `root`) e inicializa el esquema desde `diagramas/script.sql`.

### 2. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La app queda disponible en **http://localhost:8080**.

### 3. Probar el registro y los correos

1. Ve a http://localhost:8080/registro y crea una cuenta.
2. Entrarás directo a `/inicio` (sesión iniciada automáticamente).
3. Los correos de bienvenida y de recuperación se envían al **sandbox de Mailtrap** (`sandbox.smtp.mailtrap.io`). Revísalos en https://mailtrap.io → *My Inbox* (credenciales ya configuradas en `application.properties`).

> Los correos de Mailtrap solo son visibles en su panel; no llegan a un correo real. Para producción, reemplaza las credenciales por las de un SMTP real (ej. Gmail con **Contraseña de aplicación**).

---

## Rutas principales

| Ruta                      | Método | Descripción                                      |
|---------------------------|--------|--------------------------------------------------|
| `/registro`               | GET/POST | Registro de cliente con validación (auto-login) |
| `/verificar?token=...`    | GET    | Confirma la cuenta con el token (opcional)      |
| `/login`                  | GET/POST | Inicio de sesión                              |
| `/inicio`                 | GET    | Pantalla principal (sesión requerida)           |
| `/recuperar`              | GET/POST | Solicita el enlace de recuperación            |
| `/recuperar?token=...`    | GET    | Muestra el formulario de nueva contraseña       |
| `/recuperar/restablecer`  | POST   | Guarda la nueva contraseña (BCrypt)            |
| `/logout`                 | GET    | Cierra la sesión                                |

---

## Estructura del proyecto

```
src/main/java/com/gestioncitas/plataformacitas/
├── config/SecurityConfig.java      # BCrypt + configuración de seguridad
├── controladores/AuthController.java # Rutas de login, registro, verificación y recuperación
├── dto/                            # ClienteRegistroDTO, RestablecerPasswordDTO
├── modelos/                        # Entidades JPA (Usuario, Cliente, ...)
├── repositorios/                   # Interfaces Spring Data JPA
└── servicios/
    ├── UsuarioService.java         # Registro, autenticación, verificación, recuperación
    └── CorreoService.java          # Envío de correos (verificación y recuperación)

src/main/resources/
├── static/css/styles.css           # CSS propio (diseño personalizado: V3)
├── templates/                      # Vistas Thymeleaf
└── application.properties          # Configuración BD y correos
```

---

## Seguridad

- Las contraseñas se guardan **únicamente** como hash BCrypt (nunca en texto plano).
- El registro usa un **DTO** (`ClienteRegistroDTO`), por lo que no se aceptan campos extra desde el formulario (protección contra *mass assignment*).
- El login valida el hash con `PasswordEncoder.matches()`.
- Los tokens de verificación y recuperación son UUID aleatorios con expiración (24 h y 30 min respectivamente).
- La recuperación no revela si un correo existe (mensaje genérico) para evitar enumeración de cuentas.

> **Nota**: `SecurityConfig` tiene acceso abierto en todas las rutas (la sesión se valida manualmente en los controladores) y **CSRF activado** (los formularios Thymeleaf inyectan el token automáticamente). Para producción se debe restringir el acceso por rol.

---

## Pruebas

```bash
./mvnw test
```

Cubre el servicio de usuario: registro exitoso, correo duplicado, contraseñas que no coinciden, rechazo de contraseñas en texto plano, verificación de token válido y expirado, generación de token de recuperación y restablecimiento de contraseña (válido y expirado).

---

## Próximos pasos

- Módulos funcionales del sistema (servicios, citas, empleados, administración).
- Roles y permisos con Spring Security (acceso por perfil).
