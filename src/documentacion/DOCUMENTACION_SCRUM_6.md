# SCRUM-6: Gestionar horarios disponibles

Documentación para el equipo. Resume **qué hace** esta historia, **qué se implementó** y **qué archivo tocar** si hay que cambiar algo.

---

## 1. Para qué sirve (en palabras simples)

La plataforma reserva citas. Para eso necesita:

| Pieza | Pregunta que responde | Pantalla |
|---|---|---|
| Servicios | ¿Qué se ofrece? | `/admin/servicios` |
| Empleados | ¿Quién lo hace? | API de empleados |
| **Horarios** | **¿Cuándo está libre el empleado?** | **`/admin/horarios`** |
| Reserva | El cliente ocupa un hueco | `/reservar` |

Un **horario** no es una cita. Es un bloque de agenda, por ejemplo:

> Carlos Mendoza, 26-08-2026, de 08:00 a 12:00, estado `DISPONIBLE`.

Cuando el cliente elige servicio y fecha en **Reservar**, el sistema lee esos bloques en MySQL y muestra huecos (09:00, 09:30, …). Si el admin borra o cambia un bloque, el cliente lo ve al momento (no hay simulación ni caché).

---

## 2. Criterios de aceptación cubiertos

- [x] El administrador puede **agregar** horarios disponibles.
- [x] Puede **modificar** o **eliminar** horarios.
- [x] Los cambios se **reflejan** en la reserva del cliente (misma tabla).
- [x] El sistema **evita duplicados**: mismo empleado + misma fecha + misma hora de inicio y de fin.

No se tocó `reserva.js` ni la lógica de `/reservar` (`CitaService` / `CitaController`).

---

## 3. De dónde salen los datos (no es mock)

Todo vive en MySQL, base `plataforma_citas`, tabla `horarios_disponibilidad`.

Al **arrancar** la app, `DataInitializer` (SCRUM-1) inserta datos demo si ese día el empleado no tiene bloques:

- 60 días hacia adelante
- Por cada empleado: **08:00–12:00** y **13:00–18:00**
- Estado `DISPONIBLE`

Por eso el listado admin puede tener cientos de filas. Son filas reales. Crear/editar/borrar en `/admin/horarios` hace `INSERT`/`UPDATE`/`DELETE` en esa tabla.

**Si quieres menos filas demo:** edita `inicializarHorariosDisponibilidad()` en:

`src/main/java/com/gestioncitas/plataformacitas/servicios/DataInitializer.java`

(baja `diasAGenerar` o comenta la llamada). No borres la tabla a mano si no es necesario.

---

## 4. Qué archivo modificar según lo que quieras cambiar

### Quiero cambiar la **pantalla admin** (tabla, botón Nuevo, Editar/Eliminar)

| Qué | Archivo |
|---|---|
| Listado | `src/main/resources/templates/admin-horarios.html` |
| Formulario (empleado, fecha, horas) | `src/main/resources/templates/admin-horario-formulario.html` |
| Enlace del menú “Horarios” | `src/main/resources/templates/fragmentos/navegacion.html` |
| Rutas MVC `/admin/horarios` | `src/main/java/.../controladores/HorarioController.java` |
| Estilos (no inventar clases nuevas; reutilizar `app-shell`, `app-card`, `table-app`, `btn-pill`) | `src/main/resources/static/css/styles.css` |

Patrón copiado de **admin de servicios** (`ServicioController` + `admin-servicios.html`).

### Quiero cambiar las **reglas de negocio** (duplicados, alta, baja)

`src/main/java/com/gestioncitas/plataformacitas/servicios/HorarioDisponibilidadService.java`  
`src/main/java/com/gestioncitas/plataformacitas/servicios/impl/HorarioDisponibilidadServiceImpl.java`

Métodos: `crear`, `asignar` (llama a `crear`), `actualizar`, `eliminar`, `listarTodos`, `listarDisponibles`, `consultarDisponibilidad`.

Duplicado = mismo `empleadoId` + `fecha` + `horaInicio` + `horaFin` → excepción `HorarioNoDisponibleException` (HTTP **409** en REST).

**No dupliques lógica** en el controlador MVC: siempre llama al service.

### Quiero cambiar la **API REST** (Postman / frontend)

`src/main/java/com/gestioncitas/plataformacitas/controladores/HorarioRestController.java`

También existe (historia de empleados, no reescribir):

- `POST /api/empleados/{id}/horarios` → usa el mismo `asignar`/`crear` (incluye duplicados).
- `GET /api/empleados/{id}/horarios` → disponibilidad por empleado.

La reserva del cliente **sigue** usando `GET /api/citas/disponibilidad` (`CitaController`). No lo cambies para esta historia.

### Quiero cambiar el **modelo / columnas de la BD**

| Qué | Archivo |
|---|---|
| Entidad JPA | `modelos/HorarioDisponibilidad.java` |
| Enum de estados | `modelos/EstadoHorario.java` (`DISPONIBLE`, `RESERVADO`, `BLOQUEADO`) |
| Consultas | `repositorios/HorarioDisponibilidadRepository.java` |
| Body de alta/edición | `dto/HorarioRequestDTO.java` |
| Respuesta (incluye `empleadoId`, `empleadoNombre`) | `dto/HorarioResponseDTO.java` |

Hibernate está en `ddl-auto=update`. Si agregas columnas, revisa MySQL.

### Quiero cambiar el **botón de tema** (sol/luna)

Estaba `position: fixed` y se veía “caído” debajo del menú. Ahora está **dentro de la barra**.

| Qué | Archivo |
|---|---|
| Botón en el menú | `fragmentos/navegacion.html` |
| CSS: fijo en login; estático dentro de `.app-nav` | `static/css/styles.css` (`.auth-theme-toggle` y `.app-nav .auth-theme-toggle`) |

Login / registro / recuperar **sí** siguen con el botón flotante (no tienen nav).

### No tocar (otras historias)

- `reserva.js` y `templates/reservar-cita.html` (salvo el botón de tema duplicado que se quitó porque ahora está en la nav)
- `CitaService` / `CitaController` (slots de reserva)
- Admin de servicios (`ServicioController`, `admin-servicios.html`) salvo que copies el patrón

---

## 5. Rutas MVC (navegador)

Base: `http://localhost:8080`

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/admin/horarios` | Lista |
| GET | `/admin/horarios/nuevo` | Formulario alta |
| POST | `/admin/horarios` | Guardar nuevo |
| GET | `/admin/horarios/{id}/editar` | Formulario edición |
| POST | `/admin/horarios/{id}/editar` | Guardar cambios |
| POST | `/admin/horarios/{id}/eliminar` | Borrar (con confirm JS) |

---

## 6. Endpoints REST

| Método | Ruta | Descripción | HTTP |
|---|---|---|---|
| GET | `/api/horarios/disponibles` | Listar `DISPONIBLE` (opcional `?empleadoId=` y `?fecha=`) | 200 |
| POST | `/api/horarios` | Crear | 201 |
| PUT | `/api/horarios/{id}` | Editar | 200 |
| DELETE | `/api/horarios/{id}` | Eliminar | 204 |
| GET | `/api/citas/disponibilidad?servicioId=&fecha=` | Slots para **reservar** (otra historia) | 200 |

### Ejemplo POST `/api/horarios`

```json
{
  "empleadoId": 1,
  "fecha": "2026-08-28",
  "horaInicio": "09:00:00",
  "horaFin": "13:00:00"
}
```

Header: `Content-Type: application/json`

---

## 7. Archivos creados vs modificados

### Creados (SCRUM-6)

- `controladores/HorarioController.java` — MVC admin
- `controladores/HorarioRestController.java` — API
- `templates/admin-horarios.html`
- `templates/admin-horario-formulario.html`

### Modificados (SCRUM-6)

- `servicios/HorarioDisponibilidadService.java` — interfaz CRUD/listados
- `servicios/impl/HorarioDisponibilidadServiceImpl.java` — reglas + duplicados
- `repositorios/HorarioDisponibilidadRepository.java` — `existsBy…` y `findAllConEmpleado…`
- `dto/HorarioResponseDTO.java` — nombre del empleado para la tabla
- `templates/fragmentos/navegacion.html` — enlace Horarios + botón de tema
- `static/css/styles.css` — tema en la nav + scroll de slots en reserva

### Ya existían (no reescribir salvo necesidad)

- Entidad, DTOs de request, `EmpleadoController` (POST/GET de horarios anidados), `DataInitializer`

---

## 8. Cómo probar rápido

1. Arrancar MySQL (`plataforma_citas`, usuario `root`).
2. `.\mvnw.cmd spring-boot:run`
3. Login → menú **Horarios** → crear/editar/borrar.
4. Ir a **Reservar**, mismo empleado/fecha, y comprobar que los huecos coinciden.

Si no carga la app: revisar `application.properties` (URL, usuario y contraseña de MySQL).
