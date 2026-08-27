package com.gestioncitas.plataformacitas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.modelos.EstadoHorario;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
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
class DisponibilidadEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private CategoriaServicioRepository categoriaServicioRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HorarioDisponibilidadRepository horarioDisponibilidadRepository;

    @Autowired
    private CitaRepository citaRepository;

    private Servicio servicio;

    @BeforeEach
    void prepararDatos() {
        citaRepository.deleteAll();
        horarioDisponibilidadRepository.deleteAll();
        empleadoRepository.deleteAll();
        servicioRepository.deleteAll();
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

        Empleado empleado = new Empleado();
        empleado.setNombre("Carlos Mendoza (Especialista)");
        empleado.setCorreo("carlos.mendoza@empresa.com");
        empleado.setPassword("123456");
        empleado.setActivo(true);
        empleado.setEspecialidad(especialidad);
        empleado.setServicios(List.of(servicio));
        empleado = empleadoRepository.save(empleado);

        HorarioDisponibilidad bloque = new HorarioDisponibilidad();
        bloque.setEmpleado(empleado);
        bloque.setFecha(LocalDate.now().plusDays(1));
        bloque.setHoraInicio(LocalTime.of(9, 0));
        bloque.setHoraFin(LocalTime.of(10, 0));
        bloque.setEstado(EstadoHorario.DISPONIBLE.name());
        horarioDisponibilidadRepository.save(bloque);
    }

    @Test
    void endpointServiciosDevuelveCatalogo() throws Exception {
        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Consulta General"));
    }

    @Test
    void endpointServiciosFiltraPorCategoriaPrecioDuracionYTexto() throws Exception {
        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setNombre("Estética");
        categoria = categoriaServicioRepository.save(categoria);

        Servicio servicioEstetica = new Servicio();
        servicioEstetica.setNombre("Limpieza facial");
        servicioEstetica.setDescripcion("Tratamiento facial profundo");
        servicioEstetica.setPrecio(new BigDecimal("40.00"));
        servicioEstetica.setDuracionMinutos(60);
        servicioEstetica.setActivo(true);
        servicioEstetica.setCategoria(categoria);
        servicioRepository.save(servicioEstetica);

        mockMvc.perform(get("/api/servicios")
                        .param("categoria", "estética")
                        .param("precioMin", "35")
                        .param("precioMax", "45")
                        .param("duracion", "60")
                        .param("query", "facial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Limpieza facial"))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Estética"));
    }

    @Test
    void endpointDisponibilidadDevuelveSlotsParaManana() throws Exception {
        LocalDate manana = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/citas/disponibilidad")
                        .param("servicioId", servicio.getId().toString())
                        .param("fecha", manana.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].empleadoNombre").value("Carlos Mendoza (Especialista)"))
                .andExpect(jsonPath("$[0].horaInicio").value("09:00:00"))
                .andExpect(jsonPath("$[0].horaFin").value("09:30:00"));
    }
}
