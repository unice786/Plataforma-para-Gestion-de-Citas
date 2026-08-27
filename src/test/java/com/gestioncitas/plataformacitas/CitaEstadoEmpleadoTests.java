package com.gestioncitas.plataformacitas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CitaEstadoEmpleadoTests {

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
    private Empleado otroEmpleado;
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
        empleado.setNombre("Carlos Mendoza");
        empleado.setCorreo("carlos.mendoza@empresa.com");
        empleado.setPassword("123456");
        empleado.setActivo(true);
        empleado.setEspecialidad(especialidad);
        empleado.setServicios(List.of(servicio));
        empleado = empleadoRepository.save(empleado);

        otroEmpleado = new Empleado();
        otroEmpleado.setNombre("Laura Torres");
        otroEmpleado.setCorreo("laura.torres@empresa.com");
        otroEmpleado.setPassword("123456");
        otroEmpleado.setActivo(true);
        otroEmpleado.setEspecialidad(especialidad);
        otroEmpleado.setServicios(List.of(servicio));
        otroEmpleado = empleadoRepository.save(otroEmpleado);

        cliente = new Cliente();
        cliente.setNombre("Ana López");
        cliente.setCorreo("ana.lopez@ejemplo.com");
        cliente.setPassword("123456");
        cliente.setTelefono("0991234567");
        cliente.setActivo(true);
        cliente = clienteRepository.save(cliente);
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
    void confirmarCitaPendienteCambiaEstadoExitosamente() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/confirmar")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        Cita citaActual = citaRepository.findById(cita.getId()).orElseThrow();
        assertThat(citaActual.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
    }

    @Test
    void completarCitaConfirmadaCambiaEstadoExitosamente() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.CONFIRMADA, fecha, LocalTime.of(10, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/completar")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        Cita citaActual = citaRepository.findById(cita.getId()).orElseThrow();
        assertThat(citaActual.getEstado()).isEqualTo(EstadoCita.COMPLETADA);
    }

    @Test
    void cancelarCitaPendienteDesdeEmpleadoCambiaEstado() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(11, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/cancelar-empleado")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        Cita citaActual = citaRepository.findById(cita.getId()).orElseThrow();
        assertThat(citaActual.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    void noSePuedeConfirmarUnaCitaCompletada() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.COMPLETADA, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/confirmar")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void noSePuedeCompletarUnaCitaPendiente() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/completar")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void noSePuedeConfirmarCitaDeOtroEmpleado() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/confirmar")
                        .param("empleadoId", otroEmpleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void noSePuedeCancelarCitaYaCompletada() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.COMPLETADA, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/cancelar-empleado")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmarCitaGeneraNotificacionAlCliente() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        Cita cita = crearCita(EstadoCita.PENDIENTE, fecha, LocalTime.of(9, 0));

        mockMvc.perform(put("/api/citas/" + cita.getId() + "/confirmar")
                        .param("empleadoId", empleado.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk());

        assertThat(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(cliente.getId()))
                .isNotEmpty();
    }
}
