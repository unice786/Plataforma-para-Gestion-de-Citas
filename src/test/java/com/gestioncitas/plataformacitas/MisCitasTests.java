package com.gestioncitas.plataformacitas;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.EspecialidadRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MisCitasTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private CitaRepository citaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private CategoriaServicioRepository categoriaServicioRepository;
    @Autowired private HorarioDisponibilidadRepository horarioDisponibilidadRepository;

    private Cliente clienteAutenticado;
    private Cliente otroCliente;
    private Empleado empleado;
    private Servicio servicioPropio;
    private Servicio servicioAjeno;
    private LocalDate fechaProxima;
    private Cita citaEditable;
    private Cita citaAjena;

    @BeforeEach
    void prepararDatos() {
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

        servicioPropio = crearServicio(categoria, "Consulta propia");
        servicioAjeno = crearServicio(categoria, "Servicio de otro cliente");

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre("Atención general");
        especialidad = especialidadRepository.save(especialidad);

        empleado = new Empleado();
        empleado.setNombre("Especialista");
        empleado.setCorreo("especialista@ejemplo.com");
        empleado.setPassword("123456");
        empleado.setActivo(true);
        empleado.setEspecialidad(especialidad);
        empleado.setServicios(List.of(servicioPropio, servicioAjeno));
        empleado = empleadoRepository.save(empleado);

        clienteAutenticado = crearCliente("Ana Cliente", "ana@ejemplo.com");
        otroCliente = crearCliente("Bruno Cliente", "bruno@ejemplo.com");

        fechaProxima = LocalDate.now().plusDays(1);
        citaEditable = crearCita(clienteAutenticado, servicioPropio, fechaProxima,
                LocalTime.of(11, 0), EstadoCita.CONFIRMADA);
        crearCita(clienteAutenticado, servicioPropio, fechaProxima,
                LocalTime.of(9, 0), EstadoCita.PENDIENTE);
        crearCita(clienteAutenticado, servicioPropio, fechaProxima.plusDays(2),
                LocalTime.of(15, 30), EstadoCita.CANCELADA);
        citaAjena = crearCita(otroCliente, servicioAjeno, fechaProxima.plusDays(1),
                LocalTime.of(10, 0), EstadoCita.CONFIRMADA);
    }

    @Test
    void endpointRequiereUnaSesionAutenticada() throws Exception {
        mockMvc.perform(get("/api/citas/usuario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointSoloDevuelveLasCitasPropiasOrdenadasPorFechaYHora() throws Exception {
        mockMvc.perform(get("/api/citas/usuario")
                        .sessionAttr("usuario", clienteAutenticado))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].fecha").value(fechaProxima.toString()))
                .andExpect(jsonPath("$[0].hora").value("09:00:00"))
                .andExpect(jsonPath("$[0].servicioNombre").value("Consulta propia"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[1].hora").value("11:00:00"))
                .andExpect(jsonPath("$[1].estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$[2].fecha").value(fechaProxima.plusDays(2).toString()))
                .andExpect(jsonPath("$[2].estado").value("CANCELADA"));
    }

    @Test
    void cambiarLaSesionCambiaElConjuntoDeCitasSinAceptarIdsExternos() throws Exception {
        mockMvc.perform(get("/api/citas/usuario")
                        .sessionAttr("usuario", otroCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].servicioNombre").value("Servicio de otro cliente"));
    }

    @Test
    void endpointRechazaUsuariosQueNoSonClientes() throws Exception {
        mockMvc.perform(get("/api/citas/usuario")
                        .sessionAttr("usuario", empleado))
                .andExpect(status().isForbidden());
    }

    @Test
    void vistaIncluyeLosEstadosDeCargaVacioYError() throws Exception {
        mockMvc.perform(get("/mis-citas")
                        .sessionAttr("usuario", clienteAutenticado))
                .andExpect(status().isOk())
                .andExpect(view().name("mis-citas"))
                .andExpect(content().string(containsString("Cargando tus citas...")))
                .andExpect(content().string(containsString("Aún no tienes citas registradas.")))
                .andExpect(content().string(containsString("citasTablaBody")))
                .andExpect(content().string(containsString("Acciones")))
                .andExpect(content().string(containsString("/js/mis-citas.js?v=2")));
    }

    @Test
    void clientePuedeAbrirLaEdicionDeUnaCitaPropia() throws Exception {
        mockMvc.perform(get("/mis-citas/{id}/editar", citaEditable.getId())
                        .sessionAttr("usuario", clienteAutenticado))
                .andExpect(status().isOk())
                .andExpect(view().name("editar-cita-cliente"))
                .andExpect(content().string(containsString("Editar cita")))
                .andExpect(content().string(containsString("Consulta propia")));
    }

    @Test
    void clientePuedeEditarFechaYHoraDeUnaCitaPropiaDisponible() throws Exception {
        LocalDate nuevaFecha = fechaProxima.plusDays(4);
        crearHorarioDisponible(nuevaFecha, LocalTime.of(8, 0), LocalTime.of(12, 0));

        mockMvc.perform(post("/mis-citas/{id}/editar", citaEditable.getId())
                        .sessionAttr("usuario", clienteAutenticado)
                        .with(csrf())
                        .param("fecha", nuevaFecha.toString())
                        .param("hora", "10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-citas"));

        Cita actualizada = citaRepository.findById(citaEditable.getId()).orElseThrow();
        assertEquals(nuevaFecha, actualizada.getFecha());
        assertEquals(LocalTime.of(10, 0), actualizada.getHora());
    }

    @Test
    void clientePuedeEliminarUnaCitaPropia() throws Exception {
        mockMvc.perform(post("/mis-citas/{id}/eliminar", citaEditable.getId())
                        .sessionAttr("usuario", clienteAutenticado)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-citas"));

        assertFalse(citaRepository.existsById(citaEditable.getId()));
    }

    @Test
    void clienteNoPuedeEditarNiEliminarCitasAjenas() throws Exception {
        mockMvc.perform(get("/mis-citas/{id}/editar", citaAjena.getId())
                        .sessionAttr("usuario", clienteAutenticado))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/mis-citas/{id}/eliminar", citaAjena.getId())
                        .sessionAttr("usuario", clienteAutenticado)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        assertTrue(citaRepository.existsById(citaAjena.getId()));
    }

    @Test
    void vistaSinSesionRedirigeAlLogin() throws Exception {
        mockMvc.perform(get("/mis-citas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private Servicio crearServicio(CategoriaServicio categoria, String nombre) {
        Servicio servicio = new Servicio();
        servicio.setNombre(nombre);
        servicio.setDescripcion("Servicio para prueba de citas");
        servicio.setPrecio(new BigDecimal("25.00"));
        servicio.setDuracionMinutos(30);
        servicio.setActivo(true);
        servicio.setCategoria(categoria);
        return servicioRepository.save(servicio);
    }

    private Cliente crearCliente(String nombre, String correo) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setPassword("123456");
        cliente.setTelefono("70000000");
        cliente.setActivo(true);
        return clienteRepository.save(cliente);
    }

    private Cita crearCita(Cliente cliente, Servicio servicio, LocalDate fecha,
                           LocalTime hora, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setEmpleado(empleado);
        cita.setServicio(servicio);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(estado);
        return citaRepository.save(cita);
    }

    private void crearHorarioDisponible(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        HorarioDisponibilidad horario = new HorarioDisponibilidad();
        horario.setEmpleado(empleado);
        horario.setFecha(fecha);
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horario.setEstado("DISPONIBLE");
        horarioDisponibilidadRepository.save(horario);
    }
}
