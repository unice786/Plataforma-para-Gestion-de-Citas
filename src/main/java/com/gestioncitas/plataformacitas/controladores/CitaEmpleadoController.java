package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CitaEmpleadoController {

    private final EmpleadoRepository empleadoRepository;
    private final CitaRepository citaRepository;

    public CitaEmpleadoController(EmpleadoRepository empleadoRepository, CitaRepository citaRepository) {
        this.empleadoRepository = empleadoRepository;
        this.citaRepository = citaRepository;
    }

    @GetMapping("/empleado/citas")
    public String listarCitas(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || usuario.getRol() != RolUsuario.EMPLEADO) {
            return "redirect:/login";
        }

        Empleado empleado = empleadoRepository.findById(usuario.getId()).orElse(null);
        if (empleado == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado");
        }

        model.addAttribute("empleado", empleado);
        model.addAttribute("citas", citaRepository.findByEmpleadoIdOrderByFechaDescHoraDesc(empleado.getId()));
        return "empleado-citas";
    }
}
