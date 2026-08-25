package com.gestioncitas.plataformacitas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import java.math.BigDecimal;
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
class ServicioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private CategoriaServicioRepository categoriaServicioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioDisponibilidadRepository horarioDisponibilidadRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @BeforeEach
    void prepararBaseDeDatos() {
        citaRepository.deleteAll();
        horarioDisponibilidadRepository.deleteAll();
        empleadoRepository.deleteAll();
        servicioRepository.deleteAll();
        categoriaServicioRepository.deleteAll();
    }

    @Test
    void administradorPuedeCrearEditarYRetirarServicio() throws Exception {
        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setNombre("Bienestar");
        categoria = categoriaServicioRepository.save(categoria);

        mockMvc.perform(post("/admin/servicios")
                        .param("nombre", "Masaje relajante")
                        .param("categoria.id", categoria.getId().toString())
                        .param("descripcion", "Masaje de cuerpo completo")
                        .param("precio", "25.00")
                        .param("duracionMinutos", "60"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        Servicio servicio = servicioRepository.findByActivoTrueOrderByNombreAsc().getFirst();
        assertThat(servicio.getNombre()).isEqualTo("Masaje relajante");
        assertThat(servicio.getPrecio()).isEqualByComparingTo(new BigDecimal("25.00"));

        mockMvc.perform(post("/admin/servicios/{id}/editar", servicio.getId())
                        .param("nombre", "Masaje terapéutico")
                        .param("categoria.id", categoria.getId().toString())
                        .param("descripcion", "Masaje especializado")
                        .param("precio", "30.00")
                        .param("duracionMinutos", "75"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        Servicio actualizado = servicioRepository.findById(servicio.getId()).orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("Masaje terapéutico");
        assertThat(actualizado.getDuracionMinutos()).isEqualTo(75);

        mockMvc.perform(post("/admin/servicios/{id}/eliminar", servicio.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        assertThat(servicioRepository.findByActivoTrueOrderByNombreAsc()).isEmpty();
        assertThat(servicioRepository.findById(servicio.getId()).orElseThrow().getActivo()).isFalse();
    }
}
