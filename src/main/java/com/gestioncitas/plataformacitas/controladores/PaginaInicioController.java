package com.gestioncitas.plataformacitas.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaInicioController {

    @GetMapping({"/", "/index.html"})
    public String redirigirReserva() {
        return "redirect:/reserva.html";
    }
}