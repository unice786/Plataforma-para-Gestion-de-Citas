# Elaborar la Programación de las Clases de la Capa de Entidades de Negocio

Documentación de la entrega correspondiente a la capa de **Entidades de Negocio (modelo)** de la plataforma Sugoi (gestión de citas), proyecto Spring Boot.

---

## Portada

| Campo | Dato |
|---|---|
| Proyecto | Sugoi - Plataforma para Gestión de Citas |
| Materia |  Creación de Aplicaciones en Java Enterprise Edition  |
| Grupo | Grupo 5 |
| Docente | Marvin Antonio Barrera Trigueros |
| Integrantes | Michael Stanley Sanchez Menendez<br>Samuel Alonso Mendoza Calzadilla<br>Manuel Angel Carias Juarez<br>Kevin Ernesto Meza Garcia<br>Krisler David Galicia Dueñas |
| Jira | https://grupo5software.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog |
| Tecnología | Java 21, Spring Boot 3.3.4, Spring Data JPA (Hibernate), MySQL 8 |
| Fecha | 25/08/2026 |

---

## 1. Arquitectura y Estructura del Proyecto

Proyecto inicializado con Spring Boot siguiendo la estructura por capas (estructura estándar de los video tutoriales):

```
src/main/java/com/gestioncitas/plataformacitas/
├── config/        → SecurityConfig.java (BCrypt + filtro de seguridad)
├── controladores/ → AuthController, EmpleadoController, ...
├── dto/ y dtos/   → DTOs de entrada/salida con validación
├── modelos/       → Entidades de negocio (capa modelo)
├── repositorios/  → Interfaces Spring Data JPA
└── servicios/     → Reglas de negocio (interfaces + impl/)
```

### Documentación de diseño (`diagramas/`)

Carpeta `diagramas/` en la raíz del proyecto con los archivos PlantUML:

| Archivo | Descripción |
|---|---|
| `diagrama_clases.puml` | Diagrama de Clases (modelo JPA, herencia y relaciones) |
| `diagrama_bd.puml` | Diagrama Entidad-Relación (DER normalizado 3FN) |
| `script.sql` | Script SQL de creación de las tablas (10 tablas) |

Los diagramas se visualizan directamente en el README.md del repositorio mediante el proxy de PlantUML (formato del repo de ejemplo `testpuml`):

- Diagrama de Clases: `https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_clases.puml&v=4`
- Diagrama DER: `https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_bd.puml&v=4`

> **Verificado:** ambos enlaces responden `200 OK` y el proxy de PlantUML genera el PNG correctamente (los diagramas se renderizan en la página principal del repositorio).

---

## 2. Gestión en Jira y Metodología de Trabajo

Proyecto Jira: **SCRUM** (board: `grupo5software.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog`).

Historias de Usuario desglosadas en subtareas para la capa de entidades:

| HU | Historia | Subtareas de la capa de entidades |
|---|---|---|
| SCRUM-12 | Registrar cliente y login | Crear entidad `Usuario` (base, herencia JOINED); crear entidad `Cliente` (PK compartida); validaciones de correo único y contraseña |
| SCRUM-14 | Gestionar Empleados | Crear entidad `Especialidad`; crear entidad `Empleado` (`@ManyToOne` especialidad); crear entidad `HorarioDisponibilidad` (`@ManyToOne` empleado, estado por defecto `DISPONIBLE`); entidad abstracta `Usuario` con herencia `JOINED` |
| SCRUM-15 | Recuperar contraseña | Campos de token en `Usuario`: `token_verificacion`, `token_expiracion`, `token_recuperacion`, `token_recuperacion_expiracion` |
| SCRUM-1 | Reserva de citas en línea | Implementar entidades `CategoriaServicio`, `Servicio` y `Cita` con validaciones Bean Validation y relaciones (`@ManyToOne`, `@ManyToMany`); enums `EstadoCita` y `EstadoHorario`; tabla puente `empleado_servicio` |

Cada HU se trabajó en una rama propia (`feature/SCRUM-12-...`, `feature/SCRUM-14-...`, `feature/SCRUM-15-...`, `feature/citas`) y se integró a `develop` mediante Pull Request.

---

## 3. Git, GitHub y Flujo de Ramas (GitFlow)

- Repositorio vinculado: https://github.com/unice786/Plataforma-para-Gestion-de-Citas
- Flujo GitFlow implementado:

| Rama | Propósito |
|---|---|
| `main` | Producción: versión estable validada |
| `develop` | Integración: fusiones de los avances del modelo |
| `feature/SCRUM-12-registrar-cliente` | HU SCRUM-12 (registro + login) |
| `feature/SCRUM-14-gestionar-empleados` | HU SCRUM-14 (empleados y horarios) |
| `feature/SCRUM-15-recuperar-cuenta` | HU SCRUM-15 (recuperar contraseña) |
| `feature/citas` / `integration/SCRUM-1-citas` | Reserva de citas en línea (API, entidades de citas/servicios y frontend integrado) |

- **Estándar de commits** (formato `[Código_Jira] Nombre del integrante: Tarea - Descripción`). Tareas entregadas bajo este estándar:

| Tarea entregada | Cumplimiento |
|---|---|
| Registro de cliente con validación, verificación de correo con Mailtrap, autenticación por sesión, diseño de login/registro y diagramas actualizados | ✅ |
| Recuperación de contraseña con enlace temporal por correo, validación de token, expiración de 30 min y diseño de la vista | ✅ |
| Actualización de diagramas UML/DER (v4) | ✅ |
| Gestión de empleados: implementación del CRUD de empleados y horarios | ✅ |
| Reserva de citas en línea: API de servicios/citas, disponibilidad por empleado y fecha, vista integrada y tests | ✅ |
| Ampliación de datos demo (categoría Barbería y Estilo) y especialistas vinculados solo a los servicios de su rubro | ✅ |

- **README.md:** configurado con los diagramas de clases y DER embebidos (vistas directamente en la página principal del repositorio), siguiendo el formato del repositorio de ejemplo `testpuml`.

---

## 4. Desarrollo del Modelo (Entidades de Negocio)

Carpeta: `src/main/java/com/gestioncitas/plataformacitas/modelos/`. El código respeta el Diagrama de Clases y el DER (nombres de tablas, tipos de datos y relaciones JPA).

### 4.1 Entidad abstracta `Usuario`

Tabla `usuarios`, herencia `JOINED` (`@Inheritance(strategy = InheritanceType.JOINED)`), base de `Cliente`, `Empleado` y `Administrador`.

```java
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    private Boolean activo = true;
    private Boolean verificado = false;

    @Column(name = "token_verificacion")
    private String tokenVerificacion;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiracion;

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    @Column(name = "token_recuperacion_expiracion")
    private LocalDateTime tokenRecuperacionExpiracion;
}
```

### 4.2 `Cliente` (hereda de `Usuario`)

Tabla `clientes`, PK compartida con `usuarios` vía `@PrimaryKeyJoinColumn(name = "usuario_id")`.

```java
@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario {

    private String telefono;

    public Cliente() {}
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
```

### 4.3 `Empleado` (hereda de `Usuario`)

Tabla `empleados`, relación `@ManyToOne` a `Especialidad` (obligatoria) y `@OneToMany` a `HorarioDisponibilidad` con cascada.

```java
@Entity
@Table(name = "empleados")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter @Setter @NoArgsConstructor
public class Empleado extends Usuario {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioDisponibilidad> horarios = new ArrayList<>();
}
```

### 4.4 `Especialidad`

Tabla `especialidades`, `@OneToMany` a `Empleado`.

```java
@Entity
@Table(name = "especialidades")
@Getter @Setter @NoArgsConstructor
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "especialidad")
    private List<Empleado> empleados = new ArrayList<>();
}
```

### 4.5 `HorarioDisponibilidad`

Tabla `horarios_disponibilidad`, `@ManyToOne` a `Empleado`, estado por defecto `DISPONIBLE`.

```java
@Entity
@Table(name = "horarios_disponibilidad")
@Getter @Setter @NoArgsConstructor
public class HorarioDisponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 20)
    private String estado = "DISPONIBLE";
}
```

### 4.6 `Administrador` (hereda de `Usuario`)

Tabla `administradores`, PK compartida con `usuarios`. Sustenta la cuenta administrativa del sistema (creada y verificada automáticamente por `DataInitializer`).

```java
@Entity
@Table(name = "administradores")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Administrador extends Usuario {

    public Administrador() {}
}
```

### 4.7 `CategoriaServicio`

Tabla `categorias_servicio`, `@OneToMany` a `Servicio`. Agrupa el catálogo por rubro (p. ej. *Salud y Bienestar*, *Barbería y Estilo*). Validaciones Bean Validation con mensajes personalizados.

```java
@Entity
@Table(name = "categorias_servicio")
@Getter @Setter @NoArgsConstructor
public class CategoriaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    @Column(nullable = false, length = 50)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Column(length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "categoria")
    private List<Servicio> servicios = new ArrayList<>();
}
```

### 4.8 `Servicio`

Tabla `servicios`: `@ManyToOne` obligatorio a `CategoriaServicio`, `@ManyToMany` inversa con `Empleado` (tabla puente `empleado_servicio`) y `@OneToMany` a `Cita`.

```java
@Entity
@Table(name = "servicios")
@Getter @Setter @NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La categoría del servicio es obligatoria")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaServicio categoria;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    @Column(length = 1000)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "La duración es obligatoria")
    @Positive(message = "La duración debe ser mayor que cero")
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @NotNull(message = "El estado activo del servicio es obligatorio")
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(mappedBy = "servicios")
    private List<Empleado> empleados = new ArrayList<>();

    @OneToMany(mappedBy = "servicio")
    private List<Cita> citas = new ArrayList<>();
}
```

### 4.9 `Cita`

Tabla `citas`: núcleo del negocio. Relaciones obligatorias a `Cliente`, `Empleado` y `Servicio`; estado como enum (`EnumType.STRING`) con `PENDIENTE` por defecto y registro automático de la fecha de alta.

```java
@Entity
@Table(name = "citas")
@Getter @Setter @NoArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El cliente de la cita es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "El empleado de la cita es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @NotNull(message = "El servicio de la cita es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @NotNull(message = "La fecha de la cita es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotNull(message = "El estado de la cita es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @NotNull(message = "La fecha de registro es obligatoria")
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
```

### 4.10 Enums de dominio

Los estados se modelan como enums Java persistidos como `STRING` (legibles en base de datos y sin valores mágicos):

```java
public enum EstadoCita {
    PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
}

public enum EstadoHorario {
    DISPONIBLE, RESERVADO, BLOQUEADO
}
```

- `EstadoCita`: ciclo de vida de la reserva (por defecto `PENDIENTE` al crear la cita).
- `EstadoHorario`: estado de cada bloque de disponibilidad (`DISPONIBLE` por defecto; el endpoint de disponibilidad filtra por este valor).

### 4.11 Datos demo (`DataInitializer`)

Inicialización idempotente al arrancar la aplicación (perfil distinto de `test`), que demuestra el modelo completo:

| Dato | Contenido |
|---|---|
| Categorías | *Salud y Bienestar*, *Barbería y Estilo* |
| Servicios | 3 de salud + 4 de barbería (precio y duración coherentes con el rubro) |
| Especialistas | 4 empleados: 2 de atención/salud y 2 de barbería/estilo |
| Vínculos | Cada especialista asociado **solo** a los servicios de su rubro (`empleado_servicio`) |
| Disponibilidad | Bloques mañana (08:00–12:00) y tarde (13:00–18:00) para los próximos 60 días |
| Cuentas | Clientes demo con BCrypt y cuenta administrativa verificada |

### 4.12 Resumen de tablas y fidelidad al DER

| Tabla (script.sql) | Entidad | Relaciones JPA |
|---|---|---|
| `usuarios` | `Usuario` (abstracta) | Herencia `JOINED` (base) |
| `clientes` | `Cliente` | PK `usuario_id` → `usuarios` |
| `empleados` | `Empleado` | PK `usuario_id` → `usuarios`; `especialidad_id` → `especialidades`; 1..n horarios; n..m servicios |
| `especialidades` | `Especialidad` | 1..n empleados |
| `horarios_disponibilidad` | `HorarioDisponibilidad` | `empleado_id` → `empleados`; estado enum `EstadoHorario` |
| `administradores` | `Administrador` | PK `usuario_id` → `usuarios` |
| `categorias_servicio` | `CategoriaServicio` | 1..n servicios |
| `servicios` | `Servicio` | `categoria_id`; n..m empleados (`empleado_servicio`); 1..n citas |
| `citas` | `Cita` | `cliente_id` → `clientes`, `empleado_id` → `empleados`, `servicio_id` → `servicios`; estado enum `EstadoCita`; `fecha_registro` |
| `empleado_servicio` | (tabla puente) | `@ManyToMany` Empleado ↔ Servicio |

Todas las entidades están implementadas y mapeadas; el código respeta el Diagrama de Clases y el DER (nombres de tablas, tipos de datos y relaciones JPA).

### 4.13 Validaciones

- **Entidades (Bean Validation):** `@NotBlank`, `@NotNull`, `@Size`, `@Positive` y `@DecimalMin` con mensajes personalizados en `CategoriaServicio`, `Servicio` y `Cita` (validación en dos capas: DTO de entrada y entidad).
- **Base de datos:** `@Column(nullable = false)`, `unique = true` en correo, `length`/`precision` en campos numéricos y de texto (Jakarta Persistence); enums persistidos como `STRING`.
- **Servicio (reglas de negocio):** encriptado de contraseña con BCrypt, correo duplicado rechazado, `horaFin > horaInicio` en horarios, tokens con expiración (24 h verificación / 30 min recuperación), control de solapamiento de citas.
- **DTOs (Hibernate Validator):** `@NotBlank`, `@Email`, `@Size`, `@Pattern` en `ClienteRegistroDTO`, `RestablecerPasswordDTO` y los DTOs de reserva.

### 4.14 Verificación

- `./mvnw compile` → **compila sin errores** (JDK 21).
- `./mvnw test` → **17 tests, 0 fallos** (contexto Spring, registro y autenticación, tokens/recuperación, reserva de citas, disponibilidad, admin de servicios y servicio de usuarios).

---

## 5. Enlaces de la Entrega

| Recurso | Enlace | Estado |
|---|---|---|
| Repositorio GitHub | https://github.com/unice786/Plataforma-para-Gestion-de-Citas | ✅ 200 OK |
| Diagrama de Clases (README) | https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_clases.puml&v=4 | ✅ 200 OK (PNG) |
| Diagrama DER (README) | https://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/develop/diagramas/diagrama_bd.puml&v=4 | ✅ 200 OK (PNG) |
| Jira (proyecto SCRUM) | https://grupo5software.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog | ✅ |
| Archivos `.puml` (main y develop) | `https://raw.githubusercontent.com/unice786/Plataforma-para-Gestion-de-Citas/<rama>/diagramas/<archivo>.puml` | ✅ 200 OK |

---

## 6. Capturas de pantalla a incluir en el PDF

Las siguientes capturas se toman y se insertan en el documento PDF de entrega:

1. **Portada oficial** con datos del proyecto, materia e integrantes (tabla de la sección Portada).
2. **Diagrama de Clases** renderizado en el README del repositorio (https://github.com/unice786/Plataforma-para-Gestion-de-Citas).
3. **Diagrama DER** renderizado en el README del repositorio.
4. **Cada clase de la carpeta `modelos/`** (11 archivos): `Usuario.java`, `Cliente.java`, `Empleado.java`, `Especialidad.java`, `HorarioDisponibilidad.java`, `Administrador.java`, `CategoriaServicio.java`, `Servicio.java`, `Cita.java` y los enums `EstadoCita.java` / `EstadoHorario.java`. Sugerencia: abrir cada archivo en IntelliJ con el *Project Tree* visible (ramas de herencia en el panel izquierdo).
5. **Tabla `usuarios` en la consola de MySQL** (`DESCRIBE usuarios;` o `SHOW CREATE TABLE usuarios;`) para mostrar fidelidad al DER.
6. **Jira:** captura del board SCRUM con las HU SCRUM-12, SCRUM-14, SCRUM-15 y SCRUM-1 y sus subtareas de la capa de entidades.
7. **GitHub:** captura de las ramas (`main`, `develop`, `feature/SCRUM-12...`, `feature/SCRUM-14...`, `feature/SCRUM-15...`, `feature/citas`) y de un commit con el formato estándar `[Código_Jira] Nombre: Tarea`.
8. **Resultado de pruebas:** terminal con `./mvnw test` mostrando `BUILD SUCCESS` y `Tests run: 17, Failures: 0`.

---

## 7. Notas Técnicas

- El `README.md` apunta a la rama `develop` para los diagramas; `main` y `develop` contienen los mismos `.puml` (verificado por hash idéntico), por lo que el enlace es estable en ambas.
- Las entidades (`Administrador`, `CategoriaServicio`, `Servicio`, `Cita`) están **implementadas**: se completaron junto con la reserva de citas en línea (SCRUM-1) e integradas a `develop` y `main` vía PRs #15/#16; su esquema está definido en `script.sql` y en los diagramas.
- Datos demo idempotentes vía `DataInitializer`: 2 categorías, 7 servicios, 4 especialistas vinculados solo a los servicios de su rubro y disponibilidad de 60 días; se reparan automáticamente vínculos o estados inconsistentes de versiones anteriores.
- Base de datos: MySQL 8 vía Docker Compose (`mysql_citas`, esquema `plataforma_citas`), `ddl-auto=update` en `develop`/`main`.
