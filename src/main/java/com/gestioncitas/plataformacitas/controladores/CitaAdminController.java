package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import com.gestioncitas.plataformacitas.servicios.CitaService;
import com.gestioncitas.plataformacitas.dto.EdicionCitaRequestDTO;
import com.gestioncitas.plataformacitas.excepciones.HorarioNoDisponibleException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Pantalla administrativa para consultar las citas programadas (SCRUM-8). */
@Controller
@RequestMapping("/admin/citas")
public class CitaAdminController {

    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final CitaService citaService;

    public CitaAdminController(CitaRepository citaRepository, ServicioRepository servicioRepository,
                               CitaService citaService) {
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.citaService = citaService;
    }

    @GetMapping
    public String listarCitas(
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "cliente", required = false) String cliente,
            Model model,
            HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";

        String clienteBusqueda = (cliente == null || cliente.isBlank()) ? null : cliente.trim();
        model.addAttribute("citas", citaRepository.buscarParaAdministrador(fecha, clienteBusqueda));
        model.addAttribute("fecha", fecha);
        model.addAttribute("cliente", clienteBusqueda == null ? "" : clienteBusqueda);
        return "citas/lista";
    }

    @GetMapping("/{id}/editar")
    public String mostrarEdicion(@PathVariable Long id, Model model, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        var cita = citaRepository.buscarPorIdParaAdministrador(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Cita no encontrada"));
        EdicionCitaRequestDTO request = new EdicionCitaRequestDTO();
        request.setFecha(cita.getFecha());
        request.setHora(cita.getHora());
        request.setServicioId(cita.getServicio().getId());
        model.addAttribute("cita", cita);
        model.addAttribute("edicionCita", request);
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByNombreAsc());
        return "citas/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("edicionCita") EdicionCitaRequestDTO edicionCita,
                         BindingResult resultado, Model model, RedirectAttributes redirect, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        if (resultado.hasErrors()) {
            cargarFormulario(id, model, edicionCita);
            return "citas/formulario";
        }
        try {
            citaService.editarCita(id, edicionCita);
            redirect.addFlashAttribute("exito", "La cita fue actualizada correctamente.");
            return "redirect:/admin/citas";
        } catch (HorarioNoDisponibleException | IllegalArgumentException | IllegalStateException ex) {
            resultado.reject("edicion", ex.getMessage());
            cargarFormulario(id, model, edicionCita);
            return "citas/formulario";
        }
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect, HttpSession session) {
        if (!esAdmin(session)) return "redirect:/login";
        try {
            citaService.cancelarCita(id);
            redirect.addFlashAttribute("exito", "La cita fue cancelada y el cambio quedó registrado.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/citas";
    }

    private void cargarFormulario(Long id, Model model, EdicionCitaRequestDTO edicionCita) {
        var cita = citaRepository.buscarPorIdParaAdministrador(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Cita no encontrada"));
        model.addAttribute("cita", cita);
        model.addAttribute("edicionCita", edicionCita);
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByNombreAsc());
    }

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && usuario.getRol() == RolUsuario.ADMINISTRADOR;
    }
}