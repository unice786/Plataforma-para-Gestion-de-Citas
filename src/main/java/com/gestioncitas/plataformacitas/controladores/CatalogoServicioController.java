package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import jakarta.servlet.http.HttpSession;
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
        model.addAttribute("servicios", servicioRepository.findActivosConCategoriaOrderByNombreAsc());
        return "catalogo-servicios";
    }

    @GetMapping("/reservar")
    public String mostrarReserva(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || usuario.getRol() != RolUsuario.CLIENTE) {
            return "redirect:/login";
        }

        model.addAttribute("clienteId", usuario.getId());
        model.addAttribute("clienteNombre", usuario.getNombre());
        return "reservar-cita";
    }
}
