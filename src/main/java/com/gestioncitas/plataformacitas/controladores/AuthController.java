package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.ClienteRegistroDTO;
import com.gestioncitas.plataformacitas.dto.RestablecerPasswordDTO;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.servicios.CorreoService;
import com.gestioncitas.plataformacitas.servicios.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final CorreoService correoService;

    public AuthController(UsuarioService usuarioService, CorreoService correoService) {
        this.usuarioService = usuarioService;
        this.correoService = correoService;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/servicios";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String correo, @RequestParam String password, HttpSession session, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.autenticar(correo, password);

        if (usuarioOpt.isPresent()) {
            session.setAttribute("usuario", usuarioOpt.get());
            return "redirect:/inicio";
        }

        model.addAttribute("error", "Credenciales incorrectas");
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        return "inicio";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("cliente", new ClienteRegistroDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("cliente") ClienteRegistroDTO dto, BindingResult result, HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "registro";
        }

        Optional<Cliente> registrado;
        try {
            registrado = usuarioService.registrar(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }

        if (registrado.isEmpty()) {
            model.addAttribute("error", "Ya existe una cuenta con ese correo");
            return "registro";
        }

        correoService.enviarVerificacion(registrado.get());
        session.setAttribute("usuario", registrado.get());
        return "redirect:/inicio?bienvenido";
    }

    @GetMapping("/verificar")
    public String verificar(@RequestParam String token) {
        Optional<Usuario> verificado = usuarioService.verificar(token);

        if (verificado.isEmpty()) {
            return "redirect:/login?error-verificacion";
        }
        return "redirect:/login?verificado";
    }

    @GetMapping("/recuperar")
    public String mostrarRecuperar(@RequestParam(required = false) String token, Model model) {
        if (token != null && !token.isBlank() && !usuarioService.tokenRecuperacionValido(token)) {
            String error = usuarioService.tokenRecuperacionExiste(token) ? "expired" : "invalid";
            return "redirect:/recuperar?error=" + error;
        }

        RestablecerPasswordDTO dto = new RestablecerPasswordDTO();
        dto.setToken(token);
        model.addAttribute("restablecer", dto);
        model.addAttribute("token", token);
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String recuperar(@RequestParam String correo, Model model) {
        Optional<Usuario> usuarioOpt = usuarioService.solicitarRecuperacion(correo);

        if (usuarioOpt.isPresent()) {
            correoService.enviarRecuperacion(usuarioOpt.get());
        }
        model.addAttribute("mensaje", "Si el correo está registrado, recibirás las instrucciones para restablecer tu contraseña.");
        return "recuperar";
    }

    @PostMapping("/recuperar/restablecer")
    public String restablecer(@Valid @ModelAttribute("restablecer") RestablecerPasswordDTO dto, BindingResult result, Model model) {
        model.addAttribute("token", dto.getToken());

        if (result.hasErrors()) {
            return "recuperar";
        }

        try {
            Optional<Usuario> restablecido = usuarioService.restablecerPassword(dto);
            if (restablecido.isEmpty()) {
                String error = usuarioService.tokenRecuperacionExiste(dto.getToken()) ? "expired" : "invalid";
                return "redirect:/recuperar?error=" + error;
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "recuperar";
        }

        return "redirect:/login?recuperada";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}