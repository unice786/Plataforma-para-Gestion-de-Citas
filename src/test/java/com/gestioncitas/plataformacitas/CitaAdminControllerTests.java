package com.gestioncitas.plataformacitas;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.modelos.EstadoHorario;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.modelos.Usuario;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CitaAdminControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private CitaRepository citaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private EmpleadoRepository empleadoRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private CategoriaServicioRepository categoriaServicioRepository;
    @Autowired private HorarioDisponibilidadRepository horarioDisponibilidadRepository;
    @Autowired private NotificacionRepository notificacionRepository;

    private LocalDate fechaPrimera;
    private LocalDate fechaSegunda;
    private Cita citaAna;
    private MockHttpSession adminSession;

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

        Servicio servicio = new Servicio();
        servicio.setNombre("Masaje relajante");
        servicio.setDescripcion("Servicio de prueba");
        servicio.setPrecio(new BigDecimal("30.00"));
        servicio.setDuracionMinutos(45);
        servicio.setActivo(true);
        servicio.setCategoria(categoria);
        servicio = servicioRepository.save(servicio);

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre("Terapia");
        especialidad = especialidadRepository.save(especialidad);

        Empleado empleado = new Empleado();
        empleado.setNombre("Profesional de prueba");
        empleado.setCorreo("profesional@ejemplo.com");
        empleado.setPassword("123456");
        empleado.setEspecialidad(especialidad);
        empleado = empleadoRepository.save(empleado);

        crearHorariosDisponibles(empleado);

        Cliente ana = crearCliente("Ana López", "ana@ejemplo.com");
        Cliente bruno = crearCliente("Bruno Díaz", "bruno@ejemplo.com");
        fechaPrimera = LocalDate.now().plusDays(2);
        fechaSegunda = LocalDate.now().plusDays(4);
        citaAna = crearCita(ana, empleado, servicio, fechaPrimera, LocalTime.of(9, 0));
        crearCita(bruno, empleado, servicio, fechaSegunda, LocalTime.of(11, 30));

        adminSession = new MockHttpSession();
        Cliente adminUser = new Cliente();
        adminUser.setId(999L);
        adminUser.setNombre("Admin");
        adminUser.setCorreo("admin@test.com");
        adminUser.setRol(RolUsuario.ADMINISTRADOR);
        adminSession.setAttribute("usuario", adminUser);
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
    void muestraTodasLasCitasConClienteServicioFechaYHora() throws Exception {
        mockMvc.perform(get("/admin/citas").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("citas/lista"))
                .andExpect(content().string(containsString("Ana López")))
                .andExpect(content().string(containsString("Bruno Díaz")))
                .andExpect(content().string(containsString("Masaje relajante")))
                .andExpect(content().string(containsString("09:00")));
    }

    @Test
    void filtraCitasPorFecha() throws Exception {
        mockMvc.perform(get("/admin/citas").param("fecha", fechaPrimera.toString()).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ana López")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Bruno Díaz"))));
    }

    @Test
    void filtraCitasPorNombreParcialDelCliente() throws Exception {
        mockMvc.perform(get("/admin/citas").param("cliente", "bru").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bruno Díaz")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Ana López"))));
    }

    @Test
    void administradorPuedeModificarFechaHoraYServicioDeUnaCita() throws Exception {
        LocalDate nuevaFecha = fechaPrimera.plusDays(1);

        mockMvc.perform(post("/admin/citas/{id}/editar", citaAna.getId())
                        .with(csrf())
                        .session(adminSession)
                        .param("fecha", nuevaFecha.toString())
                        .param("hora", "10:15")
                        .param("servicioId", citaAna.getServicio().getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/citas"));

        Cita actualizada = citaRepository.findById(citaAna.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(nuevaFecha, actualizada.getFecha());
        org.junit.jupiter.api.Assertions.assertEquals(LocalTime.of(10, 15), actualizada.getHora());
        org.junit.jupiter.api.Assertions.assertNotNull(actualizada.getFechaUltimaModificacion());
        org.junit.jupiter.api.Assertions.assertTrue(actualizada.getDetalleUltimoCambio().contains("Cita modificada"));
    }

    @Test
    void muestraElFormularioParaModificarUnaCita() throws Exception {
        mockMvc.perform(get("/admin/citas/{id}/editar", citaAna.getId()).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("citas/formulario"))
                .andExpect(content().string(containsString("Modificar cita")));
    }

    @Test
    void rechazaUnaModificacionQueSeSolapaConOtraCita() throws Exception {
        mockMvc.perform(post("/admin/citas/{id}/editar", citaAna.getId())
                        .with(csrf())
                        .session(adminSession)
                        .param("fecha", fechaSegunda.toString())
                        .param("hora", "11:00")
                        .param("servicioId", citaAna.getServicio().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("citas/formulario"))
                .andExpect(content().string(containsString("solapa")));

        Cita sinCambios = citaRepository.findById(citaAna.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(fechaPrimera, sinCambios.getFecha());
        org.junit.jupiter.api.Assertions.assertEquals(LocalTime.of(9, 0), sinCambios.getHora());
    }

    @Test
    void administradorPuedeCancelarUnaCitaSinEliminarSuHistorial() throws Exception {
        mockMvc.perform(post("/admin/citas/{id}/cancelar", citaAna.getId()).with(csrf()).session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/citas"));

        Cita cancelada = citaRepository.findById(citaAna.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(com.gestioncitas.plataformacitas.modelos.EstadoCita.CANCELADA,
                cancelada.getEstado());
        org.junit.jupiter.api.Assertions.assertTrue(cancelada.getDetalleUltimoCambio().contains("cancelada"));
    }

    private Cliente crearCliente(String nombre, String correo) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setPassword("123456");
        cliente.setTelefono("70000000");
        return clienteRepository.save(cliente);
    }

    private Cita crearCita(Cliente cliente, Empleado empleado, Servicio servicio, LocalDate fecha, LocalTime hora) {
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setEmpleado(empleado);
        cita.setServicio(servicio);
        cita.setFecha(fecha);
        cita.setHora(hora);
        return citaRepository.save(cita);
    }
}
