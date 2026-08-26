# Guía de defensa — SCRUM 7: Gestión de servicios

## Historia de usuario

> Como administrador, quiero agregar, editar o eliminar servicios para mantener actualizada la información ofrecida.

## Objetivo

El módulo permite que el administrador mantenga el catálogo de servicios: puede crear un servicio, editar sus datos o retirarlo cuando ya no se ofrece. Los clientes ven un catálogo actualizado con los servicios activos.

## Punto de partida del proyecto

Antes de este SCRUM ya existían las entidades `Servicio` y `CategoriaServicio` y su relación. Un servicio pertenece obligatoriamente a una categoría y una categoría puede tener muchos servicios.

```text
Servicio -> categoria_id -> CategoriaServicio
```

Ejemplos: un masaje pertenece a Spa y bienestar, y un corte de cabello pertenece a Peluquería. Usé el modelo ya definido por el equipo; no creé una estructura diferente.

## Arquitectura usada

```text
Administrador
      ↓
Pantalla HTML (Thymeleaf)
      ↓
Controlador Spring MVC
      ↓
Repositorio Spring Data JPA
      ↓
Hibernate
      ↓
MySQL
```

1. El administrador llena un formulario en el navegador.
2. El controlador recibe los datos.
3. El servidor valida los datos.
4. El repositorio guarda o consulta información.
5. Hibernate transforma los objetos Java en operaciones sobre MySQL.
6. El sistema vuelve a mostrar el listado o catálogo actualizado.

## Archivos implementados

- `repositorios/ServicioRepository.java`
- `repositorios/CategoriaServicioRepository.java`
- `controladores/ServicioController.java`
- `controladores/CatalogoServicioController.java`
- `templates/admin/servicios/lista.html`
- `templates/admin/servicios/formulario.html`
- `templates/servicios/catalogo.html`
- `test/.../ServicioControllerTests.java`

Además, extendí el modelo `Servicio` con validaciones y agregué `spring-boot-starter-validation` al `pom.xml`.

## Repositorios

`ServicioRepository` y `CategoriaServicioRepository` heredan de `JpaRepository`. Esto permite usar métodos como `save()`, `findById()` y `findAll()` sin escribir SQL manualmente.

```java
public interface ServicioRepository extends JpaRepository<Servicio, Long>
```

Esto reduce código repetido, disminuye errores de SQL y se integra directamente con las entidades Java.

También agregué estas consultas:

```java
List<Servicio> findAllByOrderByNombreAsc();
List<Servicio> findByActivoTrueOrderByNombreAsc();
```

Spring interpreta esos nombres y genera las consultas. La segunda consulta obtiene solo los servicios activos ordenados por nombre; se usa en el catálogo de clientes.

## Validaciones

| Campo | Regla | Motivo |
|---|---|---|
| Nombre | Obligatorio y máximo 100 caracteres | Todo servicio debe identificarse |
| Descripción | Máximo 1000 caracteres | Evita textos demasiado largos |
| Precio | Obligatorio y mayor que cero | Evita precios negativos o cero |
| Duración | Obligatoria y mayor que cero | Evita duraciones inválidas |
| Categoría | Debe existir en la base | Mantiene una relación válida |

Ejemplo:

```java
@NotBlank(message = "El nombre es obligatorio")
private String nombre;
```

Hay validación HTML (`required`, `min`) para orientar al usuario, pero la validación importante ocurre en el servidor. Una persona podría alterar el HTML o enviar una petición manual; por eso no se debe confiar solamente en la interfaz.

## Crear un servicio

Rutas:

```text
GET  /admin/servicios/nuevo  -> muestra el formulario
POST /admin/servicios        -> valida y guarda
```

Flujo:

1. Se ingresan nombre, categoría, descripción, precio y duración.
2. Spring crea un objeto `Servicio` con esos valores.
3. Se validan los campos.
4. Se verifica que la categoría enviada exista realmente en MySQL.
5. El servicio se guarda como activo.
6. El sistema redirige al listado con un mensaje de éxito.

La categoría se consulta otra vez en la base porque el navegador no es una fuente confiable. Alguien podría enviar un ID de categoría inexistente manualmente.

## Editar un servicio

Rutas:

```text
GET  /admin/servicios/{id}/editar
POST /admin/servicios/{id}/editar
```

El sistema busca el servicio por ID. Si no existe, vuelve al listado con un mensaje de error. Si existe, permite actualizar nombre, descripción, precio, duración y categoría. El ID se conserva porque identifica al mismo registro.

## Eliminar un servicio: eliminación lógica

La eliminación no borra físicamente la fila. Cambia el campo `activo` a `false`:

```java
servicio.setActivo(false);
servicioRepository.save(servicio);
```

Esto se llama eliminación lógica o baja lógica.

### Por qué no se usó `delete()`

Una cita puede estar vinculada a un servicio. Si se borra el servicio físico, se pueden romper llaves foráneas o perder información histórica. Con eliminación lógica:

- el servicio deja de aparecer para clientes;
- se conserva la información de citas anteriores;
- se mantiene la integridad referencial;
- no se pierde información.

Frase para defender:

> Decidí usar eliminación lógica mediante el atributo `activo`, porque un servicio puede estar relacionado con citas históricas. Así se retira del catálogo sin romper la integridad referencial ni perder información previa.

La ruta de eliminación usa POST:

```text
POST /admin/servicios/{id}/eliminar
```

No se usa GET porque GET debe consultar, no modificar datos. Así se reduce el riesgo de eliminar algo por visitar un enlace accidentalmente.

## Catálogo de servicios

El administrador usa:

```text
/admin/servicios
```

Allí puede ver también los servicios retirados, porque necesita control administrativo.

El cliente usa:

```text
/servicios
```

El catálogo consulta solo `activo = true`. Por eso, al crear un servicio aparece y al retirarlo desaparece automáticamente del catálogo.

## Patrón Post/Redirect/Get

Después de crear, editar o eliminar, el controlador usa una redirección:

```text
redirect:/admin/servicios
```

Flujo:

```text
POST del formulario -> guardar -> redirección -> GET del listado
```

Esto evita que al actualizar la página el navegador reenvíe el formulario y cree registros duplicados.

Frase para defender:

> Apliqué el patrón Post/Redirect/Get para prevenir reenvíos accidentales del formulario cuando el usuario actualiza la página.

## Criterios de aceptación y cumplimiento

| Criterio | Implementación |
|---|---|
| Crear servicios | Formulario y POST `/admin/servicios` |
| Modificar servicios | Formulario de edición y POST `/{id}/editar` |
| Eliminar servicios | Botón Eliminar que realiza baja lógica |
| Reflejar cambios en catálogo | `/servicios` muestra únicamente activos |
| Evitar datos incorrectos | Validaciones de cliente y servidor |

## Pruebas realizadas

Se agregó `ServicioControllerTests`. La prueba:

1. Crea una categoría temporal.
2. Crea un servicio.
3. Comprueba que se guardó correctamente.
4. Edita nombre y duración.
5. Comprueba que se actualizaron los datos.
6. Retira el servicio.
7. Comprueba que ya no aparece entre los servicios activos.
8. Comprueba que el registro sigue en la base con `activo = false`.

Comando ejecutado:

```powershell
.\mvnw.cmd clean test
```

Resultado:

```text
Tests run: 2
Failures: 0
Errors: 0
BUILD SUCCESS
```

Las pruebas usan H2, una base temporal en memoria. Así no modifican los datos reales de MySQL y pueden ejecutarse aunque MySQL esté apagado. La aplicación normal sigue conectándose a MySQL.

También se inició la aplicación contra MySQL real. Hibernate validó el esquema y Tomcat arrancó en el puerto 8080.

## Limitación actual

No hay autenticación ni control de roles implementado en el proyecto. Las rutas administrativas se organizan bajo `/admin`, pero todavía no existe una verificación técnica que impida el acceso a otras personas. Esa restricción depende de un futuro módulo de seguridad.

Frase para defender:

> La funcionalidad administrativa está separada bajo rutas `/admin`, pero la autorización real depende del módulo de autenticación, que no estaba implementado dentro del alcance actual del proyecto.

## Guion breve para la defensa

> Implementé el CRUD de servicios usando Spring Boot, JPA, Thymeleaf y MySQL. Aproveché la entidad Servicio ya definida y agregué repositorios para persistir información sin SQL manual. Creé pantallas para listar, crear y editar servicios, y un catálogo público que solo muestra los servicios activos. Validé nombre, precio, duración y categoría en el servidor. Para eliminar, usé baja lógica con el campo activo, porque un servicio puede estar asociado a citas históricas y borrarlo físicamente podría romper la integridad de la base de datos. Finalmente, agregué una prueba automática que verifica el flujo crear, editar y retirar, y comprobé que la aplicación inicia correctamente con MySQL.

## Preguntas frecuentes

### ¿Por qué no escribiste SQL manual?

Usé Spring Data JPA porque `JpaRepository` ofrece operaciones CRUD estándar, reduce código repetido y trabaja directamente con entidades Java.

### ¿Por qué no borraste el servicio físicamente?

Porque puede estar vinculado a citas históricas. La eliminación lógica evita perder información y protege las relaciones de la base de datos.

### ¿Por qué es obligatoria una categoría?

Porque el modelo de datos define que cada servicio pertenece a una categoría. Esto organiza el catálogo y permitirá filtros por categoría en el SCRUM 18.

### ¿Qué pasa con un precio negativo?

El servidor rechaza el formulario y no guarda el servicio.

### ¿Cómo se actualiza el catálogo?

Cada vez que se abre, consulta MySQL y muestra solo servicios activos. No necesita modificar manualmente una lista separada.

### ¿Cómo verificaste que funciona?

Con pruebas automáticas de crear, editar y retirar, además del arranque de la aplicación con MySQL real.

---

# Bitácora de integración y entorno de desarrollo

Esta sección registra trabajo realizado después del SCRUM 7. Sirve para explicar decisiones técnicas del proyecto y para continuar agregando los siguientes SCRUM sin mezclar sus funcionalidades con la guía original.

## Configuración local: XAMPP en lugar de Docker

La aplicación necesita una base de datos MySQL/MariaDB. Inicialmente el proyecto incluía `docker-compose.yml` para levantar MySQL con Docker, pero en este equipo Docker Desktop no podía iniciar porque no estaba disponible su entorno de virtualización.

Se decidió usar XAMPP, que incluye MariaDB y permite ejecutar la base localmente de forma más directa. La aplicación continúa usando el mismo host y puerto:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/plataforma_citas
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:}
```

La base de datos usada es `plataforma_citas`. Con la configuración habitual de XAMPP, el usuario es `root` y no tiene contraseña. Si se configura una contraseña en XAMPP, se define temporalmente antes de ejecutar el proyecto:

```powershell
$env:DB_PASSWORD="tu_contraseña"
```

### Cambio realizado en el repositorio

- Se eliminó `docker-compose.yml`.
- Se actualizó el comentario de `application.properties` para indicar que se usa MySQL local con XAMPP.
- El cambio se guardó en el commit `b85e06a` con el mensaje `chore: configurar MySQL local sin Docker`.

Frase para defender:

> Adapté el entorno local para ejecutar MariaDB mediante XAMPP. La aplicación conserva la conexión estándar a `localhost:3306` y la base `plataforma_citas`; solamente se eliminó la dependencia de Docker para facilitar la ejecución en este equipo.

## Incidente resuelto: MariaDB se cerraba inesperadamente

Al iniciar MySQL desde XAMPP aparecía el mensaje genérico `MySQL shutdown unexpectedly`. El registro normal de MariaDB indicaba que el motor alcanzaba a iniciar, por lo que no era un problema de puerto ni de tablas de la aplicación.

Al ejecutar MariaDB en modo consola se encontró la causa real: existían archivos de replicación dañados, por ejemplo `master.info`, `relay-log.info` y archivos `mysql-relay-bin*`. Esos archivos apuntaban a registros de réplica que ya no existían. La replicación no se usa en este proyecto, pero MariaDB trataba de inicializarla y abortaba el arranque.

La solución fue mover de forma reversible 98 archivos de metadatos y registros de replicación a:

```text
C:\xampp\mysql\data\replication-backup-20260824-1441
```

No se borraron ni modificaron las carpetas de las bases de datos, incluyendo `plataforma_citas`. Después de retirar esos archivos de replicación, MariaDB respondió correctamente en el puerto `3306`.

Frase para defender:

> El fallo de XAMPP no estaba relacionado con el modelo de datos. MariaDB intentaba recuperar una configuración de replicación incompleta. Como el proyecto no utiliza réplicas, respaldé esos metadatos y los retiré del arranque sin tocar las bases de datos.

## Verificaciones ejecutadas

Se comprobaron estos puntos:

| Verificación | Resultado |
|---|---|
| MariaDB en `localhost:3306` | Disponible |
| Base `plataforma_citas` | Existe y es accesible |
| Aplicación Spring Boot | Inició en `http://localhost:8080` |
| Respuesta de la página principal | HTTP 200 |
| Pruebas automáticas | 17 pruebas, 0 fallos y 0 errores |

El comando de pruebas ejecutado fue:

```powershell
.\mvnw.cmd test
```

## Integración de SCRUM 1: reserva de citas

La funcionalidad de reserva de citas se integró en la rama `integration/SCRUM-1-citas`. Esta rama contiene la implementación principal y ajustes posteriores para asegurar datos de demostración y credenciales.

Antes de fusionarla se verificó que `main` era antecesora de la rama de integración, por lo que no había conflictos previstos. También se ejecutaron las pruebas automáticas con resultado exitoso.

El flujo usado en GitHub es:

```text
feature/SCRUM-1-citas
        ↓
integration/SCRUM-1-citas
        ↓  Pull Request y revisión
main
```

Frase para defender:

> Integré la historia de reserva de citas en una rama de integración antes de llevarla a `main`. Validé que no existieran conflictos con la rama principal y ejecuté las pruebas antes de crear la solicitud de cambios.

## Cómo continuar después de fusionar a `main`

Sí: después de que GitHub fusiona un Pull Request hacia `main`, se debe actualizar la rama local antes de comenzar el siguiente SCRUM. Así se parte de la versión más reciente compartida por el equipo.

```powershell
git switch main
git pull origin main
```

Para iniciar el siguiente SCRUM desde esa versión actualizada:

```powershell
git switch -c feature/SCRUM-8-consultar-citas
```

Si la rama del siguiente SCRUM ya existe, se actualiza con `main` antes de trabajar:

```powershell
git switch SCRUM-8/Consultar-citas-programadas
git merge main
```

Antes de hacer `push` o crear un Pull Request, se recomienda ejecutar:

```powershell
git status
.\mvnw.cmd test
```

`git status` debe mostrar únicamente los archivos que pertenecen al SCRUM actual. No se deben incluir archivos personales del IDE, como `.idea/compiler.xml`, porque son configuración local y no cambian el comportamiento del proyecto para el resto del equipo.

---

# SCRUM 8: Consulta administrativa de citas programadas

## Historia de usuario

> Como administrador, quiero consultar todas las citas programadas para llevar un control de la agenda.

## Criterios de aceptación cumplidos

| Criterio | Implementación |
|---|---|
| Listado de citas | Ruta `GET /admin/citas` |
| Cliente, servicio, fecha y hora | Tabla administrativa de citas |
| Búsqueda por fecha o cliente | Parámetros `fecha` y `cliente` en la URL y formulario de filtros |

## Decisión de arquitectura

La reserva de citas ya existía en el controlador REST `CitaController`, bajo la ruta `/api/citas`. Ese controlador responde JSON para que la pantalla de reserva consulte disponibilidad y registre una cita.

Para SCRUM 8 se creó `CitaAdminController`, que es un controlador MVC y devuelve una página HTML de Thymeleaf. No se mezclaron ambas responsabilidades:

```text
Reserva de citas:       navegador o JavaScript -> /api/citas -> JSON
Consulta administrativa: administrador -> /admin/citas -> plantilla HTML
```

Esto mantiene el código organizado: el controlador REST atiende servicios de la API y el controlador MVC atiende páginas del sistema.

## Archivos implementados

- `controladores/CitaAdminController.java`
- `repositorios/CitaRepository.java` (consulta de administración)
- `templates/admin/citas/lista.html`
- `static/css/styles.css` (estilos de la pantalla)
- `test/.../CitaAdminControllerTests.java`

## Ruta y filtros

La pantalla se consulta con:

```text
GET /admin/citas
```

Los filtros son opcionales y se pueden combinar:

```text
/admin/citas?fecha=2026-08-25
/admin/citas?cliente=Ana
/admin/citas?fecha=2026-08-25&cliente=Ana
```

En el controlador, una búsqueda de cliente vacía se transforma en `null`. Así el repositorio sabe que no debe aplicar ese filtro:

```java
String clienteBusqueda = (cliente == null || cliente.isBlank()) ? null : cliente.trim();
```

## Consulta de repositorio

Se agregó el método `buscarParaAdministrador(fecha, cliente)` a `CitaRepository`.

La consulta usa condiciones opcionales:

```sql
(:fecha IS NULL OR c.fecha = :fecha)
AND (:cliente IS NULL OR LOWER(c.cliente.nombre) LIKE ...)
```

Esto permite usar una sola consulta para los cuatro casos: sin filtros, solo fecha, solo cliente o ambos filtros.

La búsqueda por cliente usa `LOWER` y `LIKE`, por eso no diferencia entre mayúsculas y minúsculas y acepta coincidencias parciales. Por ejemplo, buscar `ana` también encuentra `Ana López`.

También se utilizaron `JOIN FETCH c.cliente` y `JOIN FETCH c.servicio`.

```text
Cita -> Cliente
     -> Servicio
```

`JOIN FETCH` carga esas relaciones en la misma consulta. La plantilla puede mostrar `cita.cliente.nombre` y `cita.servicio.nombre` sin generar una consulta adicional por cada fila. Esto evita el problema conocido como N+1 queries.

Frase para defender:

> Implementé una consulta con filtros opcionales. Si el administrador no envía fecha o cliente, la condición correspondiente no restringe los resultados. Además usé `JOIN FETCH` para cargar cliente y servicio junto con cada cita y evitar consultas repetidas al mostrar el listado.

## Interfaz administrativa

La vista `admin/citas/lista.html` contiene:

- Un formulario `GET`, para buscar sin modificar información.
- Un campo de fecha.
- Un campo de búsqueda por nombre del cliente.
- Un botón para limpiar filtros y volver a ver todas las citas.
- Una tabla que presenta cliente, correo, servicio, fecha, hora y estado.

Usar `GET` es correcto porque filtrar es una operación de consulta. Además permite que la URL contenga los filtros y que se pueda recargar o compartir sin reenviar datos.

## Pruebas realizadas

Se creó `CitaAdminControllerTests` con tres casos:

1. Comprueba que `/admin/citas` muestra todas las citas y sus datos principales.
2. Comprueba que el filtro por fecha muestra solo las citas de esa fecha.
3. Comprueba que el filtro por una parte del nombre del cliente muestra solo las coincidencias.

Comando ejecutado:

```powershell
.\mvnw.cmd test
```

Resultado final:

```text
Tests run: 20
Failures: 0
Errors: 0
BUILD SUCCESS
```

También se comprobó la aplicación en ejecución:

```text
http://localhost:8080/admin/citas -> HTTP 200
```

## Guion breve para la defensa de SCRUM 8

> Implementé una pantalla administrativa para consultar todas las citas programadas. Separé esta funcionalidad de la API de reservas mediante un controlador MVC bajo la ruta `/admin/citas`. El administrador puede filtrar por fecha, por nombre del cliente o por ambos campos a la vez. La tabla muestra cliente, servicio, fecha, hora y estado. En el repositorio utilicé una consulta con filtros opcionales y `JOIN FETCH` para obtener la información relacionada eficientemente. Finalmente, agregué pruebas para el listado y ambos filtros; toda la suite terminó con 20 pruebas exitosas.

## Preguntas frecuentes de SCRUM 8

### ¿Por qué creaste otro controlador si ya existía `CitaController`?

Porque `CitaController` es REST y devuelve JSON para el proceso de reserva. `CitaAdminController` devuelve una plantilla HTML para la pantalla del administrador. Separar ambos usos mantiene claras las responsabilidades.

### ¿Por qué los filtros son opcionales?

Porque el administrador debe poder ver todas las citas inicialmente y luego restringir la búsqueda solo cuando lo necesite.

### ¿Por qué la búsqueda de cliente admite una parte del nombre?

Porque es más práctica para el administrador. No necesita escribir el nombre completo ni respetar mayúsculas o minúsculas.

### ¿Por qué usaste GET en el formulario de búsqueda?

Porque buscar solo consulta datos y no modifica la base. GET también deja los filtros visibles en la URL.

---

# SCRUM 9: Modificar o cancelar citas

## Historia de usuario

> Como administrador, quiero modificar o cancelar citas cuando sea necesario para resolver cambios solicitados por los clientes.

## Criterios de aceptación cumplidos

| Criterio | Implementación |
|---|---|
| Editar fecha, hora o servicio | Formulario `GET /admin/citas/{id}/editar` y guardado `POST /admin/citas/{id}/editar` |
| Cancelar una cita | `POST /admin/citas/{id}/cancelar` cambia el estado a `CANCELADA` |
| Actualización automática | Después de guardar se aplica Post/Redirect/Get hacia `/admin/citas` |
| Registrar el cambio | Campos `fechaUltimaModificacion` y `detalleUltimoCambio` en `Cita` |

## Funcionamiento

En el listado administrativo, cada cita activa tiene las acciones **Editar** y **Cancelar**. La edición permite cambiar fecha, hora y servicio. Antes de guardar, `CitaService` consulta las citas activas del mismo empleado en la fecha elegida y rechaza cualquier solapamiento de horarios.

La cita que se está editando se excluye de la comprobación. Sin esa exclusión, el sistema detectaría la propia cita como conflicto aunque solo se guardaran los mismos datos.

Al cancelar no se elimina la fila. Se conserva el historial y se actualizan estos datos:

```java
cita.setEstado(EstadoCita.CANCELADA);
cita.setFechaUltimaModificacion(LocalDateTime.now());
cita.setDetalleUltimoCambio("Cita cancelada por el administrador.");
```

Frase para defender:

> Para cancelar una cita no la eliminé físicamente; cambié su estado a `CANCELADA` y guardé la fecha y el detalle de la última acción. Así se mantiene la trazabilidad y se conserva el historial de la reserva.

## Archivos principales

- `dto/EdicionCitaRequestDTO.java`: valida fecha, hora y servicio.
- `servicios/CitaService.java`: valida solapamientos, modifica y cancela.
- `controladores/CitaAdminController.java`: rutas MVC de edición y cancelación.
- `templates/admin/citas/formulario.html`: pantalla de edición.
- `modelos/Cita.java`: campos de trazabilidad.

## Pruebas realizadas

`CitaAdminControllerTests` verifica que el administrador puede modificar una cita, que se registra el cambio, y que al cancelar el registro se mantiene pero cambia a `CANCELADA`.

Ejecuta:

```powershell
.\mvnw.cmd test
```

## Guion breve para la defensa de SCRUM 9

> Implementé la modificación y cancelación de citas desde el panel administrativo. La edición permite cambiar fecha, hora y servicio, pero valida que el nuevo intervalo no se cruce con otra cita activa del mismo empleado. Para cancelar utilicé un cambio de estado a `CANCELADA` en lugar de borrar el registro, conservando el historial. Además, cada cambio guarda su fecha y una descripción de la última acción. Después de cada operación uso Post/Redirect/Get para actualizar el listado y evitar reenvíos accidentales del formulario.

## Preguntas frecuentes de SCRUM 9

### ¿Por qué no eliminaste la cita al cancelarla?

Porque forma parte del historial del cliente y de la agenda. Con el estado `CANCELADA` se conserva la trazabilidad sin que siga contando como una cita activa.

### ¿Cómo evitas que dos citas ocupen el mismo horario?

Antes de actualizar se consultan las citas activas del empleado para la fecha elegida. Se comparan los intervalos de inicio y fin; si se solapan, se rechaza el cambio.

### ¿Cómo se registra el cambio realizado?

La entidad `Cita` guarda `fechaUltimaModificacion` y `detalleUltimoCambio`. La pantalla administrativa muestra ese detalle junto a la cita.
