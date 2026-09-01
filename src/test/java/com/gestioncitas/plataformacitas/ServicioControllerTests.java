package com.gestioncitas.plataformacitas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CategoriaServicioRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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

    @BeforeEach
    void prepararBaseDeDatos() {
        servicioRepository.deleteAll();
        categoriaServicioRepository.deleteAll();
    }

    @Test
    void administradorPuedeCrearEditarYRetirarServicio() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();
        Cliente adminUser = new Cliente();
        adminUser.setId(999L);
        adminUser.setNombre("Admin");
        adminUser.setCorreo("admin@test.com");
        adminUser.setRol(RolUsuario.ADMINISTRADOR);
        adminSession.setAttribute("usuario", adminUser);

        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setNombre("Bienestar");
        categoria = categoriaServicioRepository.save(categoria);

        mockMvc.perform(post("/admin/servicios").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(adminSession)
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

        mockMvc.perform(post("/admin/servicios/{id}/editar", servicio.getId()).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(adminSession)
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

        mockMvc.perform(post("/admin/servicios/{id}/eliminar", servicio.getId()).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        assertThat(servicioRepository.findByActivoTrueOrderByNombreAsc()).isEmpty();
        assertThat(servicioRepository.findById(servicio.getId()).orElseThrow().getActivo()).isFalse();
    }

    @Test
    void administradorPuedeReactivarYServicioRetiradoNoApareceEnActivos() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();
        Cliente adminUser = new Cliente();
        adminUser.setId(999L);
        adminUser.setNombre("Admin");
        adminUser.setCorreo("admin@test.com");
        adminUser.setRol(RolUsuario.ADMINISTRADOR);
        adminSession.setAttribute("usuario", adminUser);

        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setNombre("Belleza");
        categoria = categoriaServicioRepository.save(categoria);

        Servicio servicio = new Servicio();
        servicio.setNombre("Manicure");
        servicio.setCategoria(categoria);
        servicio.setPrecio(new BigDecimal("15.00"));
        servicio.setDuracionMinutos(30);
        servicio.setActivo(true);
        servicio = servicioRepository.save(servicio);
        Long servicioId = servicio.getId();

        mockMvc.perform(post("/admin/servicios/{id}/eliminar", servicio.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(adminSession))
                .andExpect(status().is3xxRedirection());
        assertThat(servicioRepository.findByActivoTrueOrderByNombreAsc()).isEmpty();

        mockMvc.perform(post("/admin/servicios/{id}/reactivar", servicio.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        Servicio reactivado = servicioRepository.findById(servicio.getId()).orElseThrow();
        assertThat(reactivado.getActivo()).isTrue();
        assertThat(servicioRepository.findByActivoTrueOrderByNombreAsc().stream()
                .anyMatch(s -> s.getId().equals(servicioId))).isTrue();
    }

    @Test
    void edicionPuedeDesactivarElServicioDesdeElFormulario() throws Exception {
        MockHttpSession adminSession = new MockHttpSession();
        Cliente adminUser = new Cliente();
        adminUser.setId(999L);
        adminUser.setNombre("Admin");
        adminUser.setCorreo("admin@test.com");
        adminUser.setRol(RolUsuario.ADMINISTRADOR);
        adminSession.setAttribute("usuario", adminUser);

        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setNombre("Salud");
        categoria = categoriaServicioRepository.save(categoria);

        Servicio servicio = new Servicio();
        servicio.setNombre("Consulta");
        servicio.setCategoria(categoria);
        servicio.setPrecio(new BigDecimal("20.00"));
        servicio.setDuracionMinutos(30);
        servicio.setActivo(true);
        servicio = servicioRepository.save(servicio);

        mockMvc.perform(post("/admin/servicios/{id}/editar", servicio.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .session(adminSession)
                        .param("nombre", "Consulta general")
                        .param("categoria.id", categoria.getId().toString())
                        .param("precio", "22.00")
                        .param("duracionMinutos", "40")
                        .param("_activo", "on"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servicios"));

        assertThat(servicioRepository.findById(servicio.getId()).orElseThrow().getActivo()).isFalse();
    }
}
