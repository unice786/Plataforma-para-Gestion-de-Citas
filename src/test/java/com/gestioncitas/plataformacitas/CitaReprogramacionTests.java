package com.gestioncitas.plataformacitas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
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
import java.time.LocalDateTime;
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
class CitaReprogramacionTests {

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
    private Cliente otroCliente;
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

        cliente = new Cliente();
        cliente.setNombre("Ana López");
        cliente.setCorreo("ana.lopez@ejemplo.com");
        cliente.setPassword("123456");
        cliente.setTelefono("0991234567");
        cliente.setActivo(true);
        cliente = clienteRepository.save(cliente);

        otroCliente = new Cliente();
        otroCliente.setNombre("Pedro Martínez");
        otroCliente.setCorreo("pedro.martinez@ejemplo.com");
        otroCliente.setPassword("123456");
        otroCliente.setTelefono("0998765432");
        otroCliente.setActivo(true);
        otroCliente = clienteRepository.save(otroCliente);
    }

    private Cita crearCita(EstadoCita estado, LocalDate fecha, LocalTime hora) {
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setEmpleado(empleado);
        cita.setServicio(servicio);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(estado);
        cita.setFechaRegistro(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    @Test
    void reprogramarCitaCambiaFechaYHoraExitosamente() throws Exception {
        LocalDate fechaOriginal = LocalDate.now().plusDays(1);
        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fechaOriginal, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "10:00:00"
                                }
                                """.formatted(cita.getId(), cliente.getId(), nuevaFecha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cita.getId()))
                .andExpect(jsonPath("$.fecha").value(nuevaFecha.toString()))
                .andExpect(jsonPath("$.hora").value("10:00:00"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("reprogramada")));

        Cita citaActualizada = citaRepository.findById(cita.getId()).orElseThrow();
        assertThat(citaActualizada.getFecha()).isEqualTo(nuevaFecha);
        assertThat(citaActualizada.getHora()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void noSePuedeReprogramarAUnHorarioOcupado() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);

        // Crear dos citas: una a las 9:00 y otra a las 10:00
        Cita cita1 = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));
        Cita cita2 = crearCita(EstadoCita.CONFIRMADA, fecha, LocalTime.of(10, 0));

        // Intentar reprogramar cita2 a las 9:00 (ocupado por cita1)
        mockMvc.perform(put("/api/citas/" + cita2.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "09:00:00"
                                }
                                """.formatted(cita2.getId(), cliente.getId(), fecha)))
                .andExpect(status().isConflict());

        // Verificar que la cita2 no cambió
        Cita cita2Actual = citaRepository.findById(cita2.getId()).orElseThrow();
        assertThat(cita2Actual.getHora()).isEqualTo(LocalTime.of(10, 0));
        assertThat(cita2Actual.getFecha()).isEqualTo(fecha);
    }

    @Test
    void reprogramarUnaCitaNoAfectaSuPropioSlotExistente() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        // Reprogramar al mismo horario en otra fecha (debería permitirse)
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        mockMvc.perform(put("/api/citas/" + cita.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "09:00:00"
                                }
                                """.formatted(cita.getId(), cliente.getId(), nuevaFecha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value(nuevaFecha.toString()))
                .andExpect(jsonPath("$.hora").value("09:00:00"));
    }

    @Test
    void noSePuedeReprogramarUnaCitaCancelada() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        Cita cita = crearCita(EstadoCita.CANCELADA, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "10:00:00"
                                }
                                """.formatted(cita.getId(), cliente.getId(), nuevaFecha)))
                .andExpect(status().isConflict());
    }

    @Test
    void noSePuedeReprogramarUnaCitaCompletada() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        Cita cita = crearCita(EstadoCita.COMPLETADA, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "10:00:00"
                                }
                                """.formatted(cita.getId(), cliente.getId(), nuevaFecha)))
                .andExpect(status().isConflict());
    }

    @Test
    void noSePuedeReprogramarUnaCitaDeOtroCliente() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        // Intentar reprogramar con el ID de otro cliente
        mockMvc.perform(put("/api/citas/" + cita.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "10:00:00"
                                }
                                """.formatted(cita.getId(), otroCliente.getId(), nuevaFecha)))
                .andExpect(status().isNotFound());

        // Verificar que la cita no cambió
        Cita citaOriginal = citaRepository.findById(cita.getId()).orElseThrow();
        assertThat(citaOriginal.getFecha()).isEqualTo(fecha);
        assertThat(citaOriginal.getHora()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void reprogramarConFechaEnElPasadoDevuelveErrorDeValidacion() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalDate fechaPasada = LocalDate.now().minusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "10:00:00"
                                }
                                """.formatted(cita.getId(), cliente.getId(), fechaPasada)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reprogramarConIdPathYDtoNoCoincidenDevuelve400() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        // El path dice id=999 pero el DTO dice citaId=otro valor
        mockMvc.perform(put("/api/citas/999/reprogramar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citaId": %d,
                                  "clienteId": %d,
                                  "fecha": "%s",
                                  "hora": "10:00:00"
                                }
                                """.formatted(cita.getId(), cliente.getId(), LocalDate.now().plusDays(3))))
                .andExpect(status().isBadRequest());
    }
}
