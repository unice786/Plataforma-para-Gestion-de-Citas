package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final ClienteRepository clienteRepository;

    public RegistroController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "registro";
    }

    @PostMapping
    public String registrarCliente(@ModelAttribute("cliente") Cliente cliente) {
        clienteRepository.save(cliente);
        return "redirect:/registro?exito";
    }
}