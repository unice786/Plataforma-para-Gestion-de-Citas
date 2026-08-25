package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Pantalla administrativa para consultar las citas programadas (SCRUM-8). */
@Controller
@RequestMapping("/admin/citas")
public class CitaAdminController {

    private final CitaRepository citaRepository;

    public CitaAdminController(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @GetMapping
    public String listarCitas(
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "cliente", required = false) String cliente,
            Model model) {

        String clienteBusqueda = (cliente == null || cliente.isBlank()) ? null : cliente.trim();
        model.addAttribute("citas", citaRepository.buscarParaAdministrador(fecha, clienteBusqueda));
        model.addAttribute("fecha", fecha);
        model.addAttribute("cliente", clienteBusqueda == null ? "" : clienteBusqueda);
        return "admin/citas/lista";
    }
}
