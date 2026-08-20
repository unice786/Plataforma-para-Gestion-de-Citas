# SCRUM-14: Gestionar Empleados

Documentación del trabajo realizado en la historia de usuario **Gestionar datos de empleados** de la plataforma Sugoi (gestión de citas).

---

## 1. Historia de Usuario y Criterios de Aceptación

**Como** administrador del sistema,  
**quiero** dar de alta, editar y dar de baja empleados, además de asignarles horarios de trabajo,  
**para** mantener actualizado el personal disponible y consultar su disponibilidad por fecha.

### Criterios de aceptación

- [x] **Alta:** se crea un usuario/empleado nuevo con `activo = true`.
- [x] **Edición:** se modifican los datos del empleado existente **sin perder su ID**.
- [x] **Baja lógica:** se cambia `activo = false` (no se borra el registro, para no romper el historial de citas).
- [x] **Asignación de horarios:** se guardan rangos de disponibilidad (`fecha`, `horaInicio`, `horaFin`) con estado `DISPONIBLE`.
- [x] **Disponibilidad:** se consultan los horarios con estado `DISPONIBLE` de un empleado (filtro opcional por fecha).
- [x] **Listado:** `GET /api/empleados` devuelve solo empleados activos.

---

## 2. Archivos Creados / Modificados

Estructura por capas. Ruta base: `src/main/java/com/gestioncitas/plataformacitas/`.

### Modelos (`modelos/`)

| Archivo | Acción |
|---|---|
| `Usuario.java` | Entidad abstracta, tabla `usuarios`, herencia `JOINED` |
| `Especialidad.java` | Entidad, tabla `especialidades` (se eliminó el stub del paquete raíz) |
| `Empleado.java` | Extiende `Usuario`, PK `usuario_id`, `@ManyToOne` a `Especialidad` |
| `HorarioDisponibilidad.java` | `@ManyToOne` a `Empleado`; campos `fecha`, `horaInicio`, `horaFin`, `estado` |

### DTOs (`dtos/`)

| Archivo | Uso |
|---|---|
| `EmpleadoRequestDTO.java` | Alta y edición (con `jakarta.validation`) |
| `EmpleadoResponseDTO.java` | Respuesta limpia del empleado |
| `HorarioRequestDTO.java` | Asignación de horarios |
| `HorarioResponseDTO.java` | Disponibilidad |

### Repositorios (`repositorios/`)

| Archivo | Acción |
|---|---|
| `EmpleadoRepository.java` | `JpaRepository<Empleado, Long>` + unicidad de correo y `findByActivoTrue()` |
| `HorarioDisponibilidadRepository.java` | Consultas por empleado, fecha y estado `DISPONIBLE` |
| `EspecialidadRepository.java` | Soporte para vincular especialidad en el alta/edición |

### Servicios (`servicios/` y `servicios/impl/`)

| Archivo | Acción |
|---|---|
| `EmpleadoService.java` | Interfaz: crear, actualizar, dar de baja, listar activos |
| `EmpleadoServiceImpl.java` | Reglas de negocio de empleado |
| `HorarioDisponibilidadService.java` | Interfaz: asignar y consultar disponibilidad |
| `HorarioDisponibilidadServiceImpl.java` | Reglas de negocio de horarios |

### Controladores (`controladores/`)

| Archivo | Acción |
|---|---|
| `EmpleadoController.java` | `@RestController` en `/api/empleados` |

### Configuración

| Archivo | Acción |
|---|---|
| `pom.xml` | Dependencia `spring-boot-starter-validation` |
| `src/main/resources/application.properties` | URL de MySQL apuntando a `plataforma_citas` |

---

## 3. Tabla de Endpoints REST

Base URL local: `http://localhost:8080`

| Método HTTP | Ruta URL | Descripción | Estado HTTP esperado |
|---|---|---|---|
| `POST` | `/api/empleados` | Alta de empleado | `201 Created` |
| `PUT` | `/api/empleados/{id}` | Edición de empleado | `200 OK` |
| `DELETE` | `/api/empleados/{id}` | Baja lógica (`activo = false`) | `200 OK` |
| `GET` | `/api/empleados` | Listar empleados activos | `200 OK` |
| `POST` | `/api/empleados/{id}/horarios` | Asignar horario de trabajo | `201 Created` |
| `GET` | `/api/empleados/{id}/horarios` | Consultar disponibilidad | `200 OK` |
| `GET` | `/api/empleados/{id}/horarios?fecha=2026-08-18` | Disponibilidad filtrada por fecha | `200 OK` |

Header requerido en POST/PUT:

```
Content-Type: application/json
```

---

## 4. Ejemplos de Payload (JSON)

Listos para copiar y pegar en Postman. **Precondición:** debe existir una fila en `especialidades` (usar su `id` en `especialidadId`).

### POST `/api/empleados` — Alta

```json
{
  "nombre": "Ana Torres",
  "correo": "ana.torres@sugoi.com",
  "password": "ClaveSegura1",
  "telefono": "5551234567",
  "especialidadId": 1
}
```

Ejemplo de respuesta (`201`):

```json
{
  "id": 1,
  "nombre": "Ana Torres",
  "correo": "ana.torres@sugoi.com",
  "telefono": null,
  "nombreEspecialidad": "Corte",
  "activo": true
}
```

### PUT `/api/empleados/1` — Edición

```json
{
  "nombre": "Ana Torres López",
  "correo": "ana.torres@sugoi.com",
  "password": "ClaveNueva12",
  "telefono": "5559876543",
  "especialidadId": 1
}
```

### POST `/api/empleados/1/horarios` — Asignar horario

El `id` del empleado va en la URL. El body solo necesita fecha y rango horario (`horaFin` debe ser posterior a `horaInicio`).

```json
{
  "fecha": "2026-08-20",
  "horaInicio": "09:00:00",
  "horaFin": "13:00:00"
}
```

Ejemplo de respuesta (`201`):

```json
{
  "id": 1,
  "fecha": "2026-08-20",
  "horaInicio": "09:00:00",
  "horaFin": "13:00:00",
  "estado": "DISPONIBLE"
}
```

### GET `/api/empleados/1/horarios?fecha=2026-08-20` — Disponibilidad

Sin body. Devuelve la lista de horarios con estado `DISPONIBLE`.

---

## 5. Notas Técnicas

- **Base de datos:** MySQL. Esquema `plataforma_citas`. Hibernate usa `ddl-auto=validate` (el SQL crea las tablas; JPA no las altera).
- **Herencia JPA:** `Empleado` extiende `Usuario` con `InheritanceType.JOINED` y `@PrimaryKeyJoinColumn(name = "usuario_id")`, alineado al DER acordado.
- **Validación:** `jakarta.validation` (`@NotBlank`, `@Email`, `@NotNull`, `@Size`, `@AssertTrue`) en los DTOs de entrada, activada en el controlador con `@Valid`. Dependencia: `spring-boot-starter-validation`.
- **Baja lógica:** `DELETE` no elimina filas; solo pone `activo = false` para conservar citas históricas.
- **Teléfono:** el DTO de empleado lo exige en el request, pero la tabla `empleados` del SQL no tiene columna `telefono`. En la respuesta actual ese campo queda en `null` hasta que se agregue al modelo y al esquema.
- **Horarios:** no se pueden asignar a un empleado inactivo. El estado inicial del rango es `DISPONIBLE`.
