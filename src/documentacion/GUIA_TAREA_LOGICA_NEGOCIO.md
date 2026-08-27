# Guía: Construcción de las clases e Interfaces de la capa de lógica de negocio

## Estructura del documento PDF que debes entregar

### 1. Portada oficial (TÚ LA CREAS)
Incluir:
- Nombre del proyecto: **Sugoi - Plataforma para Gestión de Citas**
- Materia: [nombre de la materia]
- Grupo: [número de grupo]
- Docente: [nombre del docente]
- Integrantes: [todos los nombres del equipo]
- Repositorio: https://github.com/unice786/Plataforma-para-Gestion-de-Citas
- Jira: https://grupo5software.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog
- Tecnología: Java 21, Spring Boot 3.3.4, Spring Data JPA, MySQL 8
- Fecha: [fecha de entrega]

---

### 2. Arquitectura y Estructura del Proyecto

**Escribe un párrafo breve** describiendo la estructura por capas:

```
src/main/java/com/gestioncitas/plataformacitas/
├── config/        → SecurityConfig.java (BCrypt + CSRF)
├── controladores/ → Controladores MVC y REST
├── dto/           → DTOs de entrada/salida con validación
├── modelos/       → Entidades de negocio (JPA)
├── repositorios/  → Interfaces Spring Data JPA
├── servicios/     → Lógica de negocio (interfaces + impl/)
└── excepciones/   → Excepciones personalizadas + GlobalExceptionHandler
```

**Captura de pantalla que poner:** Abre IntelliJ IDEA con el Project Tree expandido mostrando la estructura de carpetas `servicios/`, `servicios/impl/`, `repositorios/`, `modelos/`, `controladores/`, `dto/`.

---

### 3. Diagrama de Clases actualizado

**Captura de pantalla que poner:** Abre `diagramas/diagrama_clases.puml` en IntelliJ (con el plugin PlantUML instalado) y toma captura del diagrama renderizado. O bien, usa el proxy de PlantUML:

URL para el README:
```
https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_clases.puml&v=4
```

**Descripción:** El diagrama incluye todas las interfaces de servicio y sus métodos, los repositorios con sus queries, y las dependencias entre capas.

---

### 4. Tabla de Historias de Usuario y Subtareas (Jira)

Crea una tabla como esta:

| HU | Historia | Subtareas de lógica de negocio |
|---|---|---|
| SCRUM-12 | Registrar cliente y login | `UsuarioService`: autenticar, registrar, verificar, solicitarRecuperacion, restablecerPassword |
| SCRUM-14 | Gestionar Empleados | `EmpleadoService`: crear, actualizar, darDeBaja, listarActivos |
| SCRUM-15 | Recuperar contraseña | `UsuarioService`: tokenRecuperacionExiste, tokenRecuperacionValido |
| SCRUM-1 | Reservar cita | `CitaService`: consultarDisponibilidad, reservarCita, editarCita, cancelarCita |
| SCRUM-3 | Reprogramar cita | `CitaService`: reprogramarCita, cancelarCitaCliente, eliminarCitaCliente |
| SCRUM-6 | Gestión de servicios | `ServicioController` (CRUD MVC) |
| SCRUM-8 | Citas admin | `CitaService`: listar, editar, cancelar (vista admin) |
| SCRUM-9 | Notificaciones | `NotificacionService`: crear, listarPorUsuario, contarNoLeidas, marcarComoLeida, notificarAdmins |
| SCRUM-10 | Roles y catálogo | Control de acceso por rol, filtros de servicios |
| SCRUM-14 | Gestionar horarios | `HorarioDisponibilidadService`: crear, actualizar, eliminar, listarPorEmpleado, crearEnBloque |
| SCRUM-19 | Horarios por empleado | Creación en bloque, diseño UI con tarjetas |
| SCRUM-XX | Empleado citas | `CitaService`: confirmarCita, completarCita, cancelarCitaEmpleado |

**Captura de pantalla que poner:** Ve a Jira → pestaña Board o Backlog → toma captura del tablero con las HU visibles.

---

### 5. Desarrollo de Servicios - Interfaces

**Capturas de pantalla que poner (una por cada interfaz):**

| # | Archivo | Qué capturar |
|---|---|---|
| 1 | `servicios/UsuarioService.java` | Toda la interfaz, se vean los métodos: autenticar, registrar, verificar, solicitarRecuperacion, tokenRecuperacionExiste, tokenRecuperacionValido, restablecerPassword |
| 2 | `servicios/EmpleadoService.java` | Toda la interfaz: crear, actualizar, darDeBaja, listarActivos |
| 3 | `servicios/CitaService.java` | Toda la interfaz: consultarDisponibilidad, consultarDisponibilidadRango, reservarCita, editarCita, cancelarCita, cancelarCitaCliente, reprogramarCita, eliminarCitaCliente, confirmarCita, completarCita, cancelarCitaEmpleado |
| 4 | `servicios/HorarioDisponibilidadService.java` | Toda la interfaz: crear, actualizar, eliminar, buscarPorId, listarTodos, listarPorEmpleado, listarPorRangoFechas, crearEnBloque |
| 5 | `servicios/NotificacionService.java` | Toda la interfaz: crear, listarPorUsuario, contarNoLeidas, marcarComoLeida, notificarAdmins |
| 6 | `servicios/CorreoService.java` | Toda la interfaz (si existe) |

---

### 6. Desarrollo de Servicios - Implementaciones (@Service)

**Capturas de pantalla que poner (una por cada clase impl):**

| # | Archivo | Qué capturar |
|---|---|---|
| 1 | `servicios/impl/UsuarioServiceImpl.java` | Clase con `@Service`, mostrar al menos el método `autenticar` y `registrar` con BCrypt |
| 2 | `servicios/impl/EmpleadoServiceImpl.java` | Clase con `@Service`, mostrar el CRUD |
| 3 | `servicios/impl/CitaServiceImpl.java` | Clase con `@Service`, mostrar `reservarCita` con anti-double booking y notificaciones |
| 4 | `servicios/impl/HorarioDisponibilidadServiceImpl.java` | Clase con `@Service`, mostrar `crearEnBloque` |
| 5 | `servicios/impl/NotificacionServiceImpl.java` | Clase con `@Service`, mostrar `notificarAdmins` |

**IMPORTANTE:** En cada captura debe verse ANCHO la anotación `@Service` en la declaración de la clase. Esto es lo que evalúa la rúbrica.

---

### 7. Repositorios

**Capturas de pantalla que poner:**

| # | Archivo | Qué capturar |
|---|---|---|
| 1 | `repositorios/UsuarioRepository.java` | Interface con findByCorreo, findByRol, existsByCorreo |
| 2 | `repositorios/CitaRepository.java` | Interface con las queries @Query y @EntityGraph |
| 3 | `repositorios/EmpleadoRepository.java` | Interface con existsByCorreo, findByActivoTrue |
| 4 | `repositorios/HorarioDisponibilidadRepository.java` | Interface con los métodos de búsqueda por empleado/fecha |
| 5 | `repositorios/NotificacionRepository.java` | Interface con findByUsuarioId y countNoLeidas |

---

### 8. Diagrama de Clases PlantUML (código fuente)

**Captura de pantalla que poner:** Abre `diagramas/diagrama_clases.puml` en el editor de texto (IntelliJ) y toma captura del código fuente mostrando las interfaces de servicio con sus métodos.

Esto demuestra que los métodos de las interfaces del código coinciden con los del diagrama (fidelidad al diseño).

---

### 9. Gestión en Jira

**Capturas de pantalla que poner:**

1. **Tablero Jira:** Ve a Jira → Board → captura del tablero con las columnas (To Do, In Progress, Done)
2. **Detalle de una HU:** Abre una HU (ej. SCRUM-1) → pestaña "Detalles" → captura mostrando las subtareas de lógica de negocio
3. **Backlog:** Captura del backlog con todas las HU listadas

---

### 10. Git, GitHub y Flujo de Ramas

**Capturas de pantalla que poner:**

1. **Ramas en GitHub:** Ve al repositorio → pestaña "Branches" → captura mostrando `main`, `develop`, y las ramas `feature/`
2. **Commits:** Ve a la pestaña "Commits" de `develop` → captura mostrando los mensajes con formato `[SCRUM-XX] Nombre: Tarea - Descripción`
3. **Pull Requests (opcional):** Si hay PRs, captura de al menos uno

---

### 11. Prueba de Ejecución (Spring Boot corriendo)

**Captura de pantalla que poner:**

1. **Consola de IntelliJ:** Ejecuta `PlataformaCitasApplication` → captura de la consola mostrando:
   - `Started PlataformaCitasApplication in X.XXX seconds`
   - `Tomcat started on port 8080`
2. **Navegador:** Abre `http://localhost:8080/inicio` → captura de la pantalla de bienvenida (o login)

---

### 12. Pruebas Automatizadas

**Captura de pantalla que poner:**

1. **Terminal:** Ejecuta `$env:JAVA_HOME="C:\Users\unice\.jdks\ms-21.0.12.1"; & "C:\Users\unice\proyectos\plataforma-citas\mvnw.cmd" clean test` → captura mostrando:
   - `Tests run: 42, Failures: 0, Errors: 0, Skipped: 0`
   - `BUILD SUCCESS`

---

### 13. Enlaces Requeridos

Incorpora estos enlaces en el documento:

| Recurso | Enlace |
|---|---|
| Repositorio GitHub | https://github.com/unice786/Plataforma-para-Gestion-de-Citas |
| Diagrama de Clases (README) | https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_clases.puml&v=4 |
| Diagrama DER (README) | https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_bd.puml&v=4 |
| Jira (proyecto SCRUM) | https://grupo5software.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog |

---

## Resumen de capturas necesarias (mínimo 15)

| # | Descripción | Sección |
|---|---|---|
| 1 | Estructura de carpetas del proyecto en IntelliJ | Arquitectura |
| 2 | Diagrama de clases renderizado (PlantUML) | Diagrama |
| 3 | Tablero Jira con HU | Jira |
| 4 | Detalle de HU con subtareas | Jira |
| 5 | Interfaz UsuarioService | Servicios |
| 6 | Interfaz EmpleadoService | Servicios |
| 7 | Interfaz CitaService | Servicios |
| 8 | Interfaz HorarioDisponibilidadService | Servicios |
| 9 | Interfaz NotificacionService | Servicios |
| 10 | Impl UsuarioServiceImpl (@Service visible) | Implementaciones |
| 11 | Impl CitaServiceImpl (@Service visible) | Implementaciones |
| 12 | Impl NotificacionServiceImpl (@Service visible) | Implementaciones |
| 13 | CitaRepository (queries @Query) | Repositorios |
| 14 | Código fuente del .puml en editor | Fidelidad al diseño |
| 15 | Ramas en GitHub | Git |
| 16 | Commits con formato [SCRUM-XX] | Git |
| 17 | Spring Boot ejecutándose en consola | Ejecución |
| 18 | Pantalla de bienvenida/login en navegador | Ejecución |
| 19 | Terminal con 42 tests y BUILD SUCCESS | Pruebas |
