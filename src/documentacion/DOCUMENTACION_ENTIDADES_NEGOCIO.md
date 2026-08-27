# Elaborar la Programación de las Clases de la Capa de Entidades de Negocio

Documentación de la entrega correspondiente a la capa de **Entidades de Negocio (modelo)** de la plataforma Sugoi (gestión de citas), proyecto Spring Boot.

---

## Portada

| Campo | Dato |
|---|---|
| Proyecto | Sugoi - Plataforma para Gestión de Citas |
| Materia | *[Completar: nombre de la materia]* |
| Grupo | *[Completar: número de grupo]* |
| Docente | *[Completar]* |
| Integrantes | *[Completar: nombres]* |
| Repositorio | https://github.com/unice786/Plataforma-para-Gestion-de-Citas |
| Jira | https://grupo5software.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog |
| Tecnología | Java 21, Spring Boot 3.3.4, Spring Data JPA (Hibernate), MySQL 8 |
| Fecha | *[Completar]* |

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

Cada HU se trabajó en una rama propia (`feature/SCRUM-12-...`, `feature/SCRUM-14-...`, `feature/SCRUM-15-...`) y se integró a `develop` mediante Pull Request.

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

- **Estándar de commits** (formato `[Código_Jira] Nombre Integrante: Tarea - Descripción`):

| Commit | Cumplimiento |
|---|---|
| `[SCRUM-12] May: Registro de cliente con validacion, verificacion de correo con Mailtrap, autenticacion por sesion, diseno de login/registro y diagramas actualizados` | ✅ |
| `[SCRUM-15] May: Recuperacion de contrasena con enlace temporal por correo, validacion de token, expiracion de 30 min y diseno de la vista` | ✅ |
| `[SCRUM-12] May: actualizar diagramas UML/DER (v4)` | ✅ |
| `[SCRUM-14] Manuel Carias: Gestionar empleados - Implementacion del CRUD de empleados y horarios` | ✅ |

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

### 4.6 Resumen de tablas y fidelidad al DER

| Tabla (script.sql) | Entidad | Relaciones JPA |
|---|---|---|
| `usuarios` | `Usuario` (abstracta) | Herencia `JOINED` (base) |
| `clientes` | `Cliente` | PK `usuario_id` → `usuarios` |
| `empleados` | `Empleado` | PK `usuario_id` → `usuarios`; `especialidad_id` → `especialidades`; 1..n horarios |
| `especialidades` | `Especialidad` | 1..n empleados |
| `horarios_disponibilidad` | `HorarioDisponibilidad` | `empleado_id` → `empleados` |
| `administradores` | `Administrador` | PK `usuario_id` → `usuarios` |
| `categorias_servicio` | `CategoriaServicio` | 1..n servicios |
| `servicios` | `Servicio` | `categoria_id`; n..m empleados (`empleado_servicio`) |
| `citas` | `Cita` | cliente, empleado, servicio |
| `empleado_servicio` | (tabla puente) | `@ManyToMany` Empleado ↔ Servicio |

### 4.7 Validaciones

- **Base de datos:** `@Column(nullable = false)`, `unique = true` en correo, `length` en campos de texto (Jakarta Persistence).
- **Servicio (reglas de negocio):** encriptado de contraseña con BCrypt, correo duplicado rechazado, `horaFin > horaInicio` en horarios, tokens con expiración (24 h verificación / 30 min recuperación).
- **DTOs (Hibernate Validator):** `@NotBlank`, `@Email`, `@Size`, `@Pattern` en `ClienteRegistroDTO` y `RestablecerPasswordDTO`.

### 4.8 Verificación

- `./mvnw compile` → **compila sin errores**.
- `./mvnw test` → **10 tests, 0 fallos** (registro, autenticación, tokens, recuperación).

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
4. **Cada clase de la carpeta `modelos/`** (9 archivos): `Usuario.java`, `Cliente.java`, `Empleado.java`, `Especialidad.java`, `HorarioDisponibilidad.java`, `Administrador.java`, `CategoriaServicio.java`, `Servicio.java`, `Cita.java`. Sugerencia: abrir cada archivo en IntelliJ con el *Project Tree* visible (ramas de herencia en el panel izquierdo).
5. **Tabla `usuarios` en la consola de MySQL** (`DESCRIBE usuarios;` o `SHOW CREATE TABLE usuarios;`) para mostrar fidelidad al DER.
6. **Jira:** captura del board SCRUM con las HU SCRUM-12, SCRUM-14 y SCRUM-15 y sus subtareas de la capa de entidades.
7. **GitHub:** captura de las ramas (`main`, `develop`, `feature/SCRUM-12...`, `feature/SCRUM-14...`, `feature/SCRUM-15...`) y de un commit con el formato estándar `[SCRUM-12] May: ...`.
8. **Resultado de pruebas:** terminal con `./mvnw test` mostrando `BUILD SUCCESS` y `Tests run: 10, Failures: 0`.

---

## 7. Notas Técnicas

- El `README.md` apunta a la rama `develop` para los diagramas; `main` y `develop` contienen los mismos `.puml` (verificado por hash idéntico), por lo que el enlace es estable en ambas.
- Los stubs restantes (`Administrador`, `CategoriaServicio`, `Servicio`, `Cita`) corresponden a módulos en desarrollo en ramas separadas (`feature/citas`, `feature/servicios`); su esquema ya está definido en `script.sql` y en los diagramas.
- Base de datos: MySQL 8 vía Docker Compose (`mysql_citas`, esquema `plataforma_citas`), `ddl-auto=update` en `develop`/`main`.
