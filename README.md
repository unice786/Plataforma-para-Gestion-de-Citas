# Sugoi - Plataforma para Gestión de Citas

Plataforma backend construida con **Java 21** y **Spring Boot 3.3.4** para la administración, disponibilidad y reservación de citas en línea.

---

## Diagramas del Sistema

### Diagrama de Clases
![Diagrama de Clases](https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_clases.puml&v=3)

### Diagrama Entidad-Relación (DER)
![Diagrama DER](https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_bd.puml&v=3)

---

## Módulo Implementado: Reservar Cita en Línea (`SCRUM-1`)

Se implementó de manera modular la capa backend para la historia de usuario **"Reservar cita en línea"** y consulta de disponibilidad, siguiendo una arquitectura limpia en capas (Controller → Service → Repository → Model / DTO).

### 1. Arquitectura y Componentes Creados

```
com.gestioncitas.plataformacitas/
├── dto/
│   ├── ReservaCitaRequestDTO.java       # Payload de entrada para registrar cita (Bean Validation)
│   ├── CitaResponseDTO.java             # Objeto de respuesta con confirmación de cita
│   ├── HorarioDisponibleDTO.java        # DTO con bloques horarios libres por empleado
│   └── ErrorResponseDTO.java            # Formato estándar de errores HTTP
├── excepciones/
│   ├── HorarioNoDisponibleException.java  # Excepción de solapamiento / conflicto (HTTP 409)
│   ├── RecursoNoEncontradoException.java  # Excepción de recurso inexistente (HTTP 404)
│   └── GlobalExceptionHandler.java        # Manejador @RestControllerAdvice global
├── repositorios/
│   ├── CitaRepository.java                # Búsqueda de citas activas por empleado y fecha
│   ├── EmpleadoRepository.java            # Búsqueda de empleados por servicio
│   └── HorarioDisponibilidadRepository.java # Consultas JPQL de horarios disponibles por servicio
├── servicios/
│   └── CitaService.java                   # Lógica de negocio, cálculo de slots y anti-double booking
└── controladores/
    └── CitaController.java                # REST Controller con endpoints /api/citas
```

---

### 2. Endpoints de la API REST

#### A. Consultar Disponibilidad
- **Método**: `GET`
- **Ruta**: `/api/citas/disponibilidad`
- **Descripción**: Retorna los bloques y slots libres según la duración del servicio y los horarios activos de los empleados asociados.
- **Query Parameters**:
  - `servicioId` (obligatorio, `Long`): ID del servicio.
  - `fecha` (opcional, `YYYY-MM-DD`): Fecha puntual (por defecto toma el día actual).
  - `desde` / `hasta` (opcional, `YYYY-MM-DD`): Para consultar rangos de fechas.

**Ejemplo de Petición**:
```http
GET /api/citas/disponibilidad?servicioId=1&fecha=2026-08-25
```

**Ejemplo de Respuesta (`200 OK`)**:
```json
[
  {
    "empleadoId": 2,
    "empleadoNombre": "Carlos Gómez",
    "fecha": "2026-08-25",
    "horaInicio": "09:00:00",
    "horaFin": "09:45:00"
  },
  {
    "empleadoId": 2,
    "empleadoNombre": "Carlos Gómez",
    "fecha": "2026-08-25",
    "horaInicio": "09:45:00",
    "horaFin": "10:30:00"
  }
]
```

---

#### B. Registrar Reserva de Cita
- **Método**: `POST`
- **Ruta**: `/api/citas/reservar`
- **Descripción**: Procesa y valida la reserva. Aplica validación de solapamiento (Anti-Double Booking) y persistencia atómica.
- **Request Body**:
```json
{
  "clienteId": 1,
  "empleadoId": 2,
  "servicioId": 1,
  "fecha": "2026-08-25",
  "hora": "09:00:00"
}
```

**Ejemplo de Respuesta Exitosa (`201 Created`)**:
```json
{
  "id": 15,
  "clienteNombre": "Juan Pérez",
  "empleadoNombre": "Carlos Gómez",
  "servicioNombre": "Corte de Cabello",
  "fecha": "2026-08-25",
  "hora": "09:00:00",
  "estado": "PENDIENTE",
  "fechaRegistro": "2026-08-19T13:25:00",
  "mensaje": "¡Cita reservada exitosamente! Su cita para 'Corte de Cabello' con Carlos Gómez está agendada para el 2026-08-25 a las 09:00:00."
}
```

---

### 3. Reglas de Negocio y Anti-Double Booking

1. **Validación de Intervalos (Solapamiento)**:
   - Antes de persistir una cita, el servicio calcula el rango horario `[horaInicio, horaInicio + duracionMinutos)`.
   - Consulta las citas activas (`PENDIENTE`, `CONFIRMADA`) del empleado en la fecha seleccionada.
   - Aplica la fórmula de solapamiento: `existenteInicio < nuevaFin && nuevoInicio < existenteFin`.
2. **Respuesta HTTP 409 (Conflict)**:
   - Si se detecta solapamiento, se lanza `HorarioNoDisponibleException` que el `GlobalExceptionHandler` transforma en un código `409 Conflict` con un mensaje descriptivo.
3. **Consistencia Transaccional**:
   - El método `reservarCita` está protegido con `@Transactional` para garantizar atomicidad y evitar inconsistencias concurrentes.
4. **Validación de Datos**:
   - `ReservaCitaRequestDTO` implementa `@NotNull` y `@FutureOrPresent` sobre la fecha. Las violaciones retornan un código `400 Bad Request`.

---

## Endpoints Adicionales

### C. Catálogo de Servicios
- **Método**: `GET`
- **Ruta**: `/api/servicios`
- **Descripción**: Devuelve los servicios activos ordenados alfabéticamente (usa `ServicioResponseDTO` con nombre, descripción, duración, precio y categoría).
- **Origen**: `ServicioRestController`.

**Ejemplo de Respuesta (`200 OK`)**:
```json
[
  {
    "id": 1,
    "nombre": "Consulta General y Diagnóstico",
    "descripcion": "Evaluación completa personalizada...",
    "duracionMinutos": 30,
    "precio": 25.00,
    "categoriaNombre": "Salud y Bienestar"
  }
]
```

### D. Clientes Registrados
- **Método**: `GET`
- **Ruta**: `/api/clientes`
- **Descripción**: Devuelve los clientes activos (`id`, `nombre`, `correo`, `telefono`). El frontend lo usa para precargar el selector de cliente en la reserva (`ClienteRestController`).

### E. Catálogo Público (Vista)
- **Método**: `GET`
- **Ruta**: `/servicios`
- **Descripción**: Página HTML con el catálogo de servicios activos renderizada por Thymeleaf (`CatalogoServicioController` → `templates/servicios/catalogo.html`).

### F. Registro de Clientes (Vista)
- **Método**: `GET` / `POST`
- **Ruta**: `/registro`
- **Descripción**: Formulario Thymeleaf para crear clientes (`RegistroController` → `templates/registro.html`). Al enviar guarda el `Cliente` y redirige con `?exito`.

### G. Página de Inicio
- **Método**: `GET`
- **Rutas**: `/` e `/index.html`
- **Descripción**: `PaginaInicioController` redirige a `/reserva.html` (la página de reserva es la portada funcional).

---

## Frontend: Página de Reserva (`reserva.html`)

Interfaz responsiva con Bootstrap 5 y tema claro/oscuro persistente (`localStorage`). Archivos en `src/main/resources/static/`:

- `reserva.html`: página única de reserva con pasos guiados.
- `js/app.js`: lógica del módulo (`App`):
  - Carga del catálogo de servicios (`GET /api/servicios`) con auto-selección del primero.
  - Selector de fecha bloqueando días pasados (`min = hoy`) + chips rápidos "Hoy / Mañana / +2 / +3 días".
  - Consulta de disponibilidad (`GET /api/citas/disponibilidad`) con spinner de carga y estado vacío.
  - Matriz de slots por empleado con hora formateada (AM/PM) y nombre del especialista.
  - Resumen dinámico de la reserva (servicio, fecha, horario, especialista, total).
  - Envío de reserva (`POST /api/citas/reservar`) con manejo de respuestas `201`, `409` y `400`.
  - Selector de cliente precargado (`GET /api/clientes`) y enlace "+ Registrar nuevo cliente" → `/registro`.
- `css/styles.css`: tokens de color para tema claro/oscuro (`data-theme`), inputs estilo píldora, botones, tarjetas, badges y estados de slots.

---

## Datos de Prueba: `DataInitializer`

`CommandLineRunner` que al arrancar garantiza datos de demostración en la base de datos:

- 2 **clientes** demo (`ana.lopez@ejemplo.com`, `juan.perez@ejemplo.com`, contraseña `123456`).
- 1 **categoría** ("Salud y Bienestar") y 3 **servicios** activos con precio y duración.
- 1 **especialidad** y 2 **empleados** vinculados a todos los servicios (tabla join `empleado_servicio`).
- **Bloques de disponibilidad** (turno mañana 08:00–12:00 y tarde 13:00–18:00) para los **próximos 60 días** por empleado.

El inicializador es **auto-reparador** en bases existentes: si la BD ya tiene servicios o empleados (p. ej. creados por versiones anteriores), fuerza `activo = true` en los servicios y re-vincula los servicios activos a los empleados cuando la tabla join está vacía o incompleta. Esto evita el error de catálogo/horarios vacíos en instalaciones ya migradas.

---

## Pruebas Automatizadas (Suite: 8 tests)

Se ejecutan con el perfil `test` (H2 en memoria) y están en `src/test/java/com/gestioncitas/plataformacitas/`:

| Clase | Verifica |
| --- | --- |
| `CitaReservaTests` | Reserva 201 con estado `PENDIENTE` y mensaje; horario ocupado → `409`; fecha pasada → `400`. |
| `DisponibilidadEndpointTests` | `GET /api/servicios` devuelve catálogo; `GET /api/citas/disponibilidad` devuelve slots y particiona según duración. |
| `RegistroTemplateTests` | `GET /registro` renderiza la vista sin errores. |
| `ServicioControllerTests` | CRUD admin de servicios (crear, editar, retirar) con limpieza correcta de la BD. |
| `PlataformaCitasApplicationTests` | El contexto de Spring Boot carga correctamente. |

Para ejecutarlas:
```bash
./mvnw test
```

---

## Correcciones Aplicadas

- **Anti-Double Booking (crítico)**: `CitaRepository.findCitasActivasByEmpleadoAndFecha` usaba `estado NOT IN :estados` con los estados *activos*, por lo que devolvía lo contrario y permitía reservar horarios ocupados. Corregido a `estado IN :estados`.
- **`registro.html` roto (HTTP 500)**: el formulario usaba `*{password}`/`*{confirmarPassword}` que no existen en `Cliente` (solo `contrasena`). Corregido el binding a `contrasena` y `confirmarPassword` sin binding.
- **Catálogo/horarios vacíos en BD migrada**: `DataInitializer` ahora repara servicios inactivos y tablas join `empleado_servicio` incompletas.
- **Páginas duplicadas**: se eliminaron `index.html` (raíz y estático), `app.js` y `styles.css` (raíz); la portada es `reserva.html` con redirección desde `/`.
- **Test con BOM**: se eliminó el Byte Order Mark (`\ufeff`) de `RegistroTemplateTests.java` que impedía compilar.

---

## Configuración de Base de Datos

- **Motor**: MySQL local (`jdbc:mysql://localhost:3306/plataforma_citas`), usuario `root` sin contraseña.
- **Esquema**: generado automáticamente con `spring.jpa.hibernate.ddl-auto=update`.
- Alternativamente el perfil `test` usa **H2 en memoria** (cero instalación) para las pruebas.

---

## Ejecución y Compilación

Para compilar y verificar el proyecto:
```bash
./mvnw clean compile
```

Para ejecutar la aplicación localmente:
```bash
./mvnw spring-boot:run
```

Para ejecutar las pruebas automatizadas:
```bash
./mvnw test
```
