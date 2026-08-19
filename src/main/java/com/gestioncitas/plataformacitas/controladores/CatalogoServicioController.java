package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoServicioController {

    private final ServicioRepository servicioRepository;

    public CatalogoServicioController(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @GetMapping("/servicios")
    public String mostrarCatalogo(Model model) {
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByNombreAsc());
        return "servicios/catalogo";
    }
}
