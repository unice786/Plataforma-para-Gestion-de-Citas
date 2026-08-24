package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.modelos.*;
import com.gestioncitas.plataformacitas.repositorios.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializador de datos de prueba completo.
 * Garantiza que siempre existan Clientes, Categorías, Servicios, Especialistas y
 * Bloques de Horarios de Disponibilidad (para los próximos 60 días).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final CategoriaServicioRepository categoriaRepository;
    private final ServicioRepository servicioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final EmpleadoRepository empleadoRepository;
    private final HorarioDisponibilidadRepository horarioRepository;

    public DataInitializer(ClienteRepository clienteRepository,
                           CategoriaServicioRepository categoriaRepository,
                           ServicioRepository servicioRepository,
                           EspecialidadRepository especialidadRepository,
                           EmpleadoRepository empleadoRepository,
                           HorarioDisponibilidadRepository horarioRepository) {
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.servicioRepository = servicioRepository;
        this.especialidadRepository = especialidadRepository;
        this.empleadoRepository = empleadoRepository;
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        inicializarClientes();
        inicializarCatalogoYEmpleados();
        inicializarHorariosDisponibilidad();
    }

    private void inicializarClientes() {
        if (clienteRepository.count() == 0) {
            Cliente clienteDemo = new Cliente();
            clienteDemo.setNombre("Ana López (Cliente Demo)");
            clienteDemo.setCorreo("ana.lopez@ejemplo.com");
            clienteDemo.setContrasena("123456");
            clienteDemo.setTelefono("0991234567");
            clienteDemo.setActivo(true);
            clienteRepository.save(clienteDemo);

            Cliente cliente2 = new Cliente();
            cliente2.setNombre("Juan Pérez");
            cliente2.setCorreo("juan.perez@ejemplo.com");
            cliente2.setContrasena("123456");
            cliente2.setTelefono("0987654321");
            cliente2.setActivo(true);
            clienteRepository.save(cliente2);

            System.out.println("✅ [DataInitializer] Clientes demo creados.");
        }
    }

    private void inicializarCatalogoYEmpleados() {
        // 1. Categoría
        CategoriaServicio categoria;
        if (categoriaRepository.count() == 0) {
            categoria = new CategoriaServicio();
            categoria.setNombre("Salud y Bienestar");
            categoria.setDescripcion("Servicios generales de atención, cuidado personal y bienestar.");
            categoria = categoriaRepository.save(categoria);
        } else {
            categoria = categoriaRepository.findAll().get(0);
        }

        // 2. Servicios
        List<Servicio> servicios = servicioRepository.findAll();
        if (servicios.isEmpty()) {
            Servicio s1 = new Servicio();
            s1.setNombre("Consulta General y Diagnóstico");
            s1.setDescripcion("Evaluación completa personalizada con recomendaciones profesionales.");
            s1.setPrecio(new BigDecimal("25.00"));
            s1.setDuracionMinutos(30);
            s1.setActivo(true);
            s1.setCategoria(categoria);
            s1 = servicioRepository.save(s1);

            Servicio s2 = new Servicio();
            s2.setNombre("Sesión Terapéutica Integral");
            s2.setDescripcion("Tratamiento especializado integral de relajación y cuidado.");
            s2.setPrecio(new BigDecimal("40.00"));
            s2.setDuracionMinutos(45);
            s2.setActivo(true);
            s2.setCategoria(categoria);
            s2 = servicioRepository.save(s2);

            Servicio s3 = new Servicio();
            s3.setNombre("Revisión y Seguimiento Premium");
            s3.setDescripcion("Control detallado de progreso con plan de acción.");
            s3.setPrecio(new BigDecimal("30.00"));
            s3.setDuracionMinutos(30);
            s3.setActivo(true);
            s3.setCategoria(categoria);
            s3 = servicioRepository.save(s3);

            servicios = List.of(s1, s2, s3);
            System.out.println("✅ [DataInitializer] Servicios creados.");
        } else {
            // Reparar servicios existentes (p.ej. columna `activo` recién agregada
            // quedó en NULL en tablas creadas por versiones anteriores).
            for (Servicio s : servicios) {
                if (s.getActivo() == null || !s.getActivo()) {
                    s.setActivo(true);
                    servicioRepository.save(s);
                }
            }
            System.out.println("✅ [DataInitializer] Servicios existentes verificados (activos).");
        }

        // 3. Especialidad
        Especialidad especialidad;
        if (especialidadRepository.count() == 0) {
            especialidad = new Especialidad();
            especialidad.setNombre("Especialista en Atención");
            especialidad.setDescripcion("Profesional certificado con experiencia en atención al cliente.");
            especialidad = especialidadRepository.save(especialidad);
        } else {
            especialidad = especialidadRepository.findAll().get(0);
        }

        // 4. Empleados y vinculación con servicios
        List<Empleado> empleados = empleadoRepository.findAll();
        if (empleados.isEmpty()) {
            Empleado emp1 = new Empleado();
            emp1.setNombre("Carlos Mendoza (Especialista)");
            emp1.setCorreo("carlos.mendoza@empresa.com");
            emp1.setContrasena("123456");
            emp1.setActivo(true);
            emp1.setEspecialidad(especialidad);
            emp1.setServicios(new ArrayList<>(servicios));
            empleadoRepository.save(emp1);

            Empleado emp2 = new Empleado();
            emp2.setNombre("Dra. Sofía Herrera");
            emp2.setCorreo("sofia.herrera@empresa.com");
            emp2.setContrasena("123456");
            emp2.setActivo(true);
            emp2.setEspecialidad(especialidad);
            emp2.setServicios(new ArrayList<>(servicios));
            empleadoRepository.save(emp2);

            System.out.println("✅ [DataInitializer] Especialistas creados y vinculados a los servicios.");
        } else {
            // Asegurar que los empleados existentes tengan asignados todos los servicios.
            // Se reasigna SIEMPRE para reparar tablas join `empleado_servicio` vacías o
            // desactualizadas en bases de datos creadas por versiones anteriores.
            for (Empleado emp : empleados) {
                List<Servicio> actuales = emp.getServicios();
                boolean incompleto = actuales == null || actuales.isEmpty();
                if (!incompleto) {
                    List<Long> idsActuales = actuales.stream().map(Servicio::getId).toList();
                    incompleto = servicios.stream().anyMatch(s -> !idsActuales.contains(s.getId()));
                }
                if (incompleto) {
                    emp.setServicios(new ArrayList<>(servicios));
                    empleadoRepository.save(emp);
                }
            }
        }
    }

    private void inicializarHorariosDisponibilidad() {
        List<Empleado> empleados = empleadoRepository.findAll();
        if (empleados.isEmpty()) return;

        LocalDate hoy = LocalDate.now();
        int diasAGenerar = 60; // Generar disponibilidad para los próximos 60 días

        int horariosCreados = 0;
        for (int i = 0; i <= diasAGenerar; i++) {
            LocalDate fecha = hoy.plusDays(i);

            for (Empleado emp : empleados) {
                // Verificar si ya tiene bloques en esa fecha
                List<HorarioDisponibilidad> existentes = horarioRepository
                        .findDisponiblesByServicioAndFecha(
                                emp.getServicios().isEmpty() ? 1L : emp.getServicios().get(0).getId(),
                                fecha,
                                EstadoHorario.DISPONIBLE
                        );

                if (existentes.isEmpty()) {
                    // Turno Mañana: 08:00 - 12:00
                    HorarioDisponibilidad bloque1 = new HorarioDisponibilidad();
                    bloque1.setEmpleado(emp);
                    bloque1.setFecha(fecha);
                    bloque1.setHoraInicio(LocalTime.of(8, 0));
                    bloque1.setHoraFin(LocalTime.of(12, 0));
                    bloque1.setEstado(EstadoHorario.DISPONIBLE);
                    horarioRepository.save(bloque1);

                    // Turno Tarde: 13:00 - 18:00
                    HorarioDisponibilidad bloque2 = new HorarioDisponibilidad();
                    bloque2.setEmpleado(emp);
                    bloque2.setFecha(fecha);
                    bloque2.setHoraInicio(LocalTime.of(13, 0));
                    bloque2.setHoraFin(LocalTime.of(18, 0));
                    bloque2.setEstado(EstadoHorario.DISPONIBLE);
                    horarioRepository.save(bloque2);

                    horariosCreados += 2;
                }
            }
        }

        if (horariosCreados > 0) {
            System.out.println("✅ [DataInitializer] Se generaron " + horariosCreados + " bloques de disponibilidad para los próximos 60 días.");
        }
    }
}
