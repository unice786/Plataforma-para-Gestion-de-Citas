package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.modelos.EstadoHorario;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.EspecialidadRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Inicializador de datos de prueba (SCRUM-1).
 * Datos base y adaptacion al modelo actual: Sam Alonso.
 * Ampliacion (categoria Barberia y Estilo, servicios demo y
 * especialistas vinculados por rubro): May Menendez.
 * Garantiza que existan clientes demo, catálogo de servicios,
 * especialistas y bloques de disponibilidad para los próximos 60 días.
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final ServicioRepository servicioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final EmpleadoRepository empleadoRepository;
    private final HorarioDisponibilidadRepository horarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(ClienteRepository clienteRepository,
                           CategoriaServicioRepository categoriaRepository,
                           ServicioRepository servicioRepository,
                           EspecialidadRepository especialidadRepository,
                           EmpleadoRepository empleadoRepository,
                           HorarioDisponibilidadRepository horarioRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.servicioRepository = servicioRepository;
        this.especialidadRepository = especialidadRepository;
        this.empleadoRepository = empleadoRepository;
        this.horarioRepository = horarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        inicializarClientes();
        inicializarAdmin();
        inicializarCatalogoYEmpleados();
        inicializarHorariosDisponibilidad();
    }

    private void inicializarClientes() {
        if (clienteRepository.count() > 0) {
            System.out.println("[DataInitializer] Clientes ya existentes, saltando inicialización.");
            return;
        }

        crearCliente("María González", "maria.gonzalez@ejemplo.com", "0987654321");
        crearCliente("José Rodríguez", "jose.rodriguez@ejemplo.com", "0955555555");
        crearCliente("Laura Martínez", "laura.martinez@ejemplo.com", "0933333333");

        System.out.println("[DataInitializer] Clientes demo creados con BCrypt.");
    }

    private void inicializarAdmin() {
        String correoAdmin = "unice891@gmail.com";

        // Verificar si el usuario admin ya existe
        Optional<Usuario> existingUser = usuarioRepository.findByCorreo(correoAdmin);

        if (existingUser.isPresent()) {
            // Usuario ya existe - actualizarlo a admin
            Usuario usuario = existingUser.get();
            usuario.setNombre("Administrador");
            usuario.setActivo(true);
            usuario.setVerificado(true);
            usuario.setTokenVerificacion(null);
            usuario.setTokenExpiracion(null);
            usuario.setTokenRecuperacion(null);
            usuario.setTokenRecuperacionExpiracion(null);
            usuarioRepository.save(usuario);

            System.out.println("[DataInitializer] Usuario " + correoAdmin + " actualizado a admin.");
        } else {
            // Usuario no existe - crear nuevo admin como Cliente (Usuario es abstracta)
            Cliente admin = new Cliente();
            admin.setNombre("Administrador");
            admin.setCorreo(correoAdmin);
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setActivo(true);
            admin.setVerificado(true);
            admin.setTelefono("0999999999");
            clienteRepository.save(admin);

            System.out.println("[DataInitializer] Cuenta admin creada con correo " + correoAdmin + ".");
        }
    }

    private void crearCliente(String nombre, String correo, String telefono) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setTelefono(telefono);
        cliente.setPassword(passwordEncoder.encode("123456"));
        cliente.setActivo(true);
        cliente.setVerificado(true);
        clienteRepository.save(cliente);
    }

    private void inicializarCatalogoYEmpleados() {
        // 1. Categorías (se crean solo si no existen por nombre)
        CategoriaServicio categoriaSalud = obtenerOCrearCategoria(
                "Salud y Bienestar",
                "Servicios generales de atención, cuidado personal y bienestar.");
        CategoriaServicio categoriaBarberia = obtenerOCrearCategoria(
                "Barbería y Estilo",
                "Cortes de cabello, arreglo de barba y cuidado del estilo personal.");

        // 2. Servicios demo (se crean solo si no existen por nombre)
        List<Servicio> existentes = servicioRepository.findAll();
        Set<String> nombresExistentes = new HashSet<>();
        for (Servicio s : existentes) {
            nombresExistentes.add(s.getNombre().toLowerCase());
        }

        List<Servicio> servicios = new ArrayList<>(existentes);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Consulta General y Diagnóstico",
                "Evaluación completa personalizada con recomendaciones profesionales.",
                new BigDecimal("25.00"), 30, categoriaSalud);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Sesión Terapéutica Integral",
                "Tratamiento especializado integral de relajación y cuidado.",
                new BigDecimal("40.00"), 45, categoriaSalud);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Revisión y Seguimiento Premium",
                "Control detallado de progreso con plan de acción.",
                new BigDecimal("30.00"), 30, categoriaSalud);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Corte Clásico",
                "Corte de cabello a tijera y máquina con lavado incluido.",
                new BigDecimal("12.00"), 30, categoriaBarberia);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Corte + Perfilado de Barba",
                "Paquete completo de corte de cabello y arreglo de barba.",
                new BigDecimal("18.00"), 45, categoriaBarberia);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Arreglo de Barba",
                "Perfilado y recorte de barba con toalla caliente.",
                new BigDecimal("8.00"), 20, categoriaBarberia);
        crearServicioSiNoExiste(servicios, nombresExistentes, "Tinte o Color",
                "Aplicación de color o tinte con productos profesionales.",
                new BigDecimal("25.00"), 60, categoriaBarberia);

        // Reparar servicios existentes con activo en NULL o retirados por versiones anteriores
        for (Servicio s : servicioRepository.findAll()) {
            if (s.getActivo() == null || !s.getActivo()) {
                s.setActivo(true);
                servicioRepository.save(s);
            }
        }
        System.out.println("[DataInitializer] Catálogo de servicios verificado (" + servicios.size() + " servicios).");

        // 3. Especialidades
        Especialidad especialidadAtencion = obtenerOCrearEspecialidad(
                "Especialista en Atención",
                "Profesional certificado con experiencia en atención al cliente.");
        Especialidad especialidadBarberia = obtenerOCrearEspecialidad(
                "Barbero Profesional",
                "Barbero y estilista certificado en cortes y cuidado personal.");

        // 4. Empleados demo (se crean solo si no existe el correo)
        crearEmpleadoSiNoExiste("Carlos Mendoza (Especialista)", "carlos.mendoza@empresa.com", especialidadAtencion);
        crearEmpleadoSiNoExiste("Dra. Sofía Herrera", "sofia.herrera@empresa.com", especialidadAtencion);
        crearEmpleadoSiNoExiste("Luis Ramírez (Barbero)", "luis.ramirez@empresa.com", especialidadBarberia);
        crearEmpleadoSiNoExiste("Valentina Cruz (Estilista)", "valentina.cruz@empresa.com", especialidadBarberia);

        // 5. Servicios agrupados por rubro
        List<Servicio> serviciosSalud = servicios.stream()
                .filter(s -> s.getCategoria().getId().equals(categoriaSalud.getId()))
                .toList();
        List<Servicio> serviciosBarberia = servicios.stream()
                .filter(s -> s.getCategoria().getId().equals(categoriaBarberia.getId()))
                .toList();

        Map<String, List<Servicio>> serviciosPorCorreo = Map.of(
                "carlos.mendoza@empresa.com", serviciosSalud,
                "sofia.herrera@empresa.com", serviciosSalud,
                "luis.ramirez@empresa.com", serviciosBarberia,
                "valentina.cruz@empresa.com", serviciosBarberia
        );

        // 6. Vincular cada especialista solo con los servicios de su rubro
        //    (repara también vínculos incorrectos de versiones anteriores)
        for (Empleado emp : empleadoRepository.findAll()) {
            List<Servicio> correctos = serviciosPorCorreo.get(emp.getCorreo());
            if (correctos == null) {
                continue;
            }

            Set<Long> idsCorrectos = new HashSet<>();
            for (Servicio s : correctos) {
                idsCorrectos.add(s.getId());
            }

            boolean difiere = emp.getServicios() == null
                    || emp.getServicios().size() != idsCorrectos.size()
                    || emp.getServicios().stream()
                            .map(Servicio::getId)
                            .anyMatch(id -> !idsCorrectos.contains(id));

            if (difiere) {
                emp.setServicios(new ArrayList<>(correctos));
                empleadoRepository.save(emp);
            }
        }
        System.out.println("[DataInitializer] Especialistas verificados y vinculados a los servicios de su rubro.");
    }

    private CategoriaServicio obtenerOCrearCategoria(String nombre, String descripcion) {
        return categoriaRepository.findAll().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> {
                    CategoriaServicio categoria = new CategoriaServicio();
                    categoria.setNombre(nombre);
                    categoria.setDescripcion(descripcion);
                    return categoriaRepository.save(categoria);
                });
    }

    private Especialidad obtenerOCrearEspecialidad(String nombre, String descripcion) {
        return especialidadRepository.findAll().stream()
                .filter(e -> e.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Especialidad especialidad = new Especialidad();
                    especialidad.setNombre(nombre);
                    especialidad.setDescripcion(descripcion);
                    return especialidadRepository.save(especialidad);
                });
    }

    private void crearServicioSiNoExiste(List<Servicio> servicios, Set<String> nombresExistentes,
                                         String nombre, String descripcion, BigDecimal precio,
                                         int duracionMinutos, CategoriaServicio categoria) {
        if (nombresExistentes.contains(nombre.toLowerCase())) {
            return;
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion(descripcion);
        servicio.setPrecio(precio);
        servicio.setDuracionMinutos(duracionMinutos);
        servicio.setActivo(true);
        servicio.setCategoria(categoria);
        servicio = servicioRepository.save(servicio);

        servicios.add(servicio);
        nombresExistentes.add(nombre.toLowerCase());
        System.out.println("[DataInitializer] Servicio demo creado: " + nombre + ".");
    }

    private void crearEmpleadoSiNoExiste(String nombre, String correo, Especialidad especialidad) {
        if (empleadoRepository.existsByCorreo(correo)) {
            return;
        }

        Empleado empleado = new Empleado();
        empleado.setNombre(nombre);
        empleado.setCorreo(correo);
        empleado.setPassword(passwordEncoder.encode("123456"));
        empleado.setActivo(true);
        empleado.setVerificado(true);
        empleado.setEspecialidad(especialidad);
        empleadoRepository.save(empleado);
        System.out.println("[DataInitializer] Especialista creado: " + nombre + ".");
    }

    private void inicializarHorariosDisponibilidad() {
        List<Empleado> empleados = empleadoRepository.findAll();
        if (empleados.isEmpty()) return;

        LocalDate hoy = LocalDate.now();
        int diasAGenerar = 60;

        int horariosCreados = 0;
        for (int i = 0; i <= diasAGenerar; i++) {
            LocalDate fecha = hoy.plusDays(i);

            for (Empleado emp : empleados) {
                List<HorarioDisponibilidad> existentes = horarioRepository
                        .findByEmpleadoIdAndFechaAndEstado(emp.getId(), fecha, EstadoHorario.DISPONIBLE.name());

                if (existentes.isEmpty()) {
                    HorarioDisponibilidad bloqueManana = new HorarioDisponibilidad();
                    bloqueManana.setEmpleado(emp);
                    bloqueManana.setFecha(fecha);
                    bloqueManana.setHoraInicio(LocalTime.of(8, 0));
                    bloqueManana.setHoraFin(LocalTime.of(12, 0));
                    bloqueManana.setEstado(EstadoHorario.DISPONIBLE.name());
                    horarioRepository.save(bloqueManana);

                    HorarioDisponibilidad bloqueTarde = new HorarioDisponibilidad();
                    bloqueTarde.setEmpleado(emp);
                    bloqueTarde.setFecha(fecha);
                    bloqueTarde.setHoraInicio(LocalTime.of(13, 0));
                    bloqueTarde.setHoraFin(LocalTime.of(18, 0));
                    bloqueTarde.setEstado(EstadoHorario.DISPONIBLE.name());
                    horarioRepository.save(bloqueTarde);

                    horariosCreados += 2;
                }
            }
        }

        if (horariosCreados > 0) {
            System.out.println("[DataInitializer] Se generaron " + horariosCreados + " bloques de disponibilidad para los próximos 60 días.");
        }
    }
}
