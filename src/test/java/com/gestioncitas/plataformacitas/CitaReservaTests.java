package com.gestioncitas.plataformacitas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import com.gestioncitas.plataformacitas.modelos.EstadoHorario;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.EspecialidadRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
import com.gestioncitas.plataformacitas.repositorios.NotificacionRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CitaReservaTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private CategoriaServicioRepository categoriaServicioRepository;

    @Autowired
    private HorarioDisponibilidadRepository horarioDisponibilidadRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    private Cliente cliente;
    private Empleado empleado;
    private Servicio servicio;

    @BeforeEach
    void prepararDatos() {
        notificacionRepository.deleteAll();
        citaRepository.deleteAll();
        horarioDisponibilidadRepository.deleteAll();
        empleadoRepository.deleteAll();
        servicioRepository.deleteAll();
        clienteRepository.deleteAll();
        especialidadRepository.deleteAll();
        categoriaServicioRepository.deleteAll();

        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setNombre("Bienestar");
        categoria = categoriaServicioRepository.save(categoria);

        servicio = new Servicio();
        servicio.setNombre("Consulta General");
        servicio.setDescripcion("Evaluación completa");
        servicio.setPrecio(new BigDecimal("25.00"));
        servicio.setDuracionMinutos(30);
        servicio.setActivo(true);
        servicio.setCategoria(categoria);
        servicio = servicioRepository.save(servicio);

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre("Especialista en Atención");
        especialidad = especialidadRepository.save(especialidad);

        empleado = new Empleado();
        empleado.setNombre("Carlos Mendoza (Especialista)");
        empleado.setCorreo("carlos.mendoza@empresa.com");
        empleado.setPassword("123456");
        empleado.setActivo(true);
        empleado.setEspecialidad(especialidad);
        empleado.setServicios(List.of(servicio));
        empleado = empleadoRepository.save(empleado);

        crearHorariosDisponibles(empleado);

        cliente = new Cliente();
        cliente.setNombre("Ana López");
        cliente.setCorreo("ana.lopez@ejemplo.com");
        cliente.setPassword("123456");
        cliente.setTelefono("0991234567");
        cliente.setActivo(true);
        cliente = clienteRepository.save(cliente);
    }

    private void crearHorariosDisponibles(Empleado emp) {
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i <= 7; i++) {
            LocalDate fecha = hoy.plusDays(i);
            horarioDisponibilidadRepository.save(crearBloque(emp, fecha,
                    LocalTime.of(8, 0), LocalTime.of(12, 0)));
            horarioDisponibilidadRepository.save(crearBloque(emp, fecha,
                    LocalTime.of(13, 0), LocalTime.of(17, 0)));
        }
    }

    private HorarioDisponibilidad crearBloque(Empleado emp, LocalDate fecha,
                                              LocalTime inicio, LocalTime fin) {
        HorarioDisponibilidad bloque = new HorarioDisponibilidad();
        bloque.setEmpleado(emp);
        bloque.setFecha(fecha);
        bloque.setHoraInicio(inicio);
        bloque.setHoraFin(fin);
        bloque.setEstado(EstadoHorario.DISPONIBLE.name());
        return bloque;
    }

    @Test
    void reservarCitaRegistraLaCitaYDevuelveMensajeDeConfirmacion() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);

        mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": %d,
                                  "empleadoId": %d,
                                  "servicioId": %d,
                                  "fecha": "%s",
                                  "hora": "09:00:00"
                                }
                                """.formatted(cliente.getId(), empleado.getId(), servicio.getId(), fecha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clienteNombre").value("Ana López"))
                .andExpect(jsonPath("$.empleadoNombre").value("Carlos Mendoza (Especialista)"))
                .andExpect(jsonPath("$.servicioNombre").value("Consulta General"))
                .andExpect(jsonPath("$.fecha").value(fecha.toString()))
                .andExpect(jsonPath("$.hora").value("09:00:00"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Cita reservada")));

        // Verificar persistencia en BD con su estado
        List<Cita> citas = citaRepository.findAll();
        assertThat(citas).hasSize(1);
        Cita cita = citas.get(0);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.PENDIENTE);
        assertThat(cita.getFecha()).isEqualTo(fecha);
        assertThat(cita.getHora()).isEqualTo(LocalTime.of(9, 0));
        assertThat(cita.getServicio().getId()).isEqualTo(servicio.getId());
        assertThat(cita.getEmpleado().getId()).isEqualTo(empleado.getId());
        assertThat(cita.getCliente().getId()).isEqualTo(cliente.getId());
    }

    @Test
    void noSePuedeReservarUnHorarioOcupado() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        String body = """
                {
                  "clienteId": %d,
                  "empleadoId": %d,
                  "servicioId": %d,
                  "fecha": "%s",
                  "hora": "09:00:00"
                }
                """.formatted(cliente.getId(), empleado.getId(), servicio.getId(), fecha);

        // Primera reserva: debe tener éxito (201)
        mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Segunda reserva en el mismo horario y empleado: debe rechazarse (409)
        mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        // Solo debe existir una cita en BD
        assertThat(citaRepository.findAll()).hasSize(1);
    }

    @Test
    void noReservaFueraDeLosBloquesDeDisponibilidadDelEmpleado() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);

        // 12:30 cae en el receso de almuerzo (12:00-13:00), fuera de los bloques 08-12 y 13-17.
        mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": %d,
                                  "empleadoId": %d,
                                  "servicioId": %d,
                                  "fecha": "%s",
                                  "hora": "12:30:00"
                                }
                                """.formatted(cliente.getId(), empleado.getId(), servicio.getId(), fecha)))
                .andExpect(status().isConflict());

        assertThat(citaRepository.findAll()).isEmpty();
    }

    @Test
    void reservarConFechaEnElPasadoDevuelveErrorDeValidacion() throws Exception {
        LocalDate fechaPasada = LocalDate.now().minusDays(1);

        mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": %d,
                                  "empleadoId": %d,
                                  "servicioId": %d,
                                  "fecha": "%s",
                                  "hora": "09:00:00"
                                }
                                """.formatted(cliente.getId(), empleado.getId(), servicio.getId(), fechaPasada)))
                .andExpect(status().isBadRequest());

        assertThat(citaRepository.findAll()).isEmpty();
    }
}