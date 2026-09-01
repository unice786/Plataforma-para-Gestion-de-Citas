package com.gestioncitas.plataformacitas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class CitaCancelacionClienteTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private CitaRepository citaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private CategoriaServicioRepository categoriaServicioRepository;
    @Autowired private HorarioDisponibilidadRepository horarioDisponibilidadRepository;
    @Autowired private NotificacionRepository notificacionRepository;

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

        CategoriaServicio cat = new CategoriaServicio();
        cat.setNombre("Bienestar");
        cat = categoriaServicioRepository.save(cat);

        servicio = new Servicio();
        servicio.setNombre("Consulta");
        servicio.setDescripcion("Test");
        servicio.setPrecio(new BigDecimal("25.00"));
        servicio.setDuracionMinutos(30);
        servicio.setActivo(true);
        servicio.setCategoria(cat);
        servicio = servicioRepository.save(servicio);

        Especialidad esp = new Especialidad();
        esp.setNombre("Medicina");
        esp = especialidadRepository.save(esp);

        empleado = new Empleado();
        empleado.setNombre("Dr. Test");
        empleado.setCorreo("dr@test.com");
        empleado.setPassword("123456");
        empleado.setActivo(true);
        empleado.setEspecialidad(esp);
        empleado.setServicios(List.of(servicio));
        empleado = empleadoRepository.save(empleado);

        crearHorariosDisponibles(empleado);

        cliente = new Cliente();
        cliente.setNombre("Ana Test");
        cliente.setCorreo("ana@test.com");
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
    void cancelarCitaClienteDevuelveExito() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);

        String bodyReserva = """
                {
                  "clienteId": %d,
                  "empleadoId": %d,
                  "servicioId": %d,
                  "fecha": "%s",
                  "hora": "09:00:00"
                }
                """.formatted(cliente.getId(), empleado.getId(), servicio.getId(), fecha);

        String respuestaReserva = mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyReserva))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number citaIdNum = com.jayway.jsonpath.JsonPath.read(respuestaReserva, "$.id");
        Long citaId = citaIdNum.longValue();

        mockMvc.perform(delete("/api/citas/" + citaId + "/cancelar")
                        .param("clienteId", cliente.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.mensaje").value(
                        org.hamcrest.Matchers.containsString("cancelada")));

        Cita cita = citaRepository.findById(citaId).orElseThrow();
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Test
    void cancelarCitaDeOtroClienteFalla() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);

        String bodyReserva = """
                {
                  "clienteId": %d,
                  "empleadoId": %d,
                  "servicioId": %d,
                  "fecha": "%s",
                  "hora": "09:00:00"
                }
                """.formatted(cliente.getId(), empleado.getId(), servicio.getId(), fecha);

        String respuestaReserva = mockMvc.perform(post("/api/citas/reservar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyReserva))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number citaIdNum = com.jayway.jsonpath.JsonPath.read(respuestaReserva, "$.id");
        Long citaId = citaIdNum.longValue();

        Cliente otroCliente = new Cliente();
        otroCliente.setNombre("Otro");
        otroCliente.setCorreo("otro@test.com");
        otroCliente.setPassword("123456");
        otroCliente.setTelefono("0999999999");
        otroCliente.setActivo(true);
        otroCliente = clienteRepository.save(otroCliente);

        mockMvc.perform(delete("/api/citas/" + citaId + "/cancelar")
                        .param("clienteId", otroCliente.getId().toString())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
