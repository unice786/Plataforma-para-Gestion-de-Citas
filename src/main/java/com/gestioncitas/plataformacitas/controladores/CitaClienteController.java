package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/clientes/{clienteId}/citas")
public class CitaClienteController {

    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;

    public CitaClienteController(ClienteRepository clienteRepository, CitaRepository citaRepository) {
        this.clienteRepository = clienteRepository;
        this.citaRepository = citaRepository;
    }

    @GetMapping
    public String listarCitas(@PathVariable Long clienteId, Model model) {
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        model.addAttribute("cliente", cliente);
        // Se consulta en cada carga para reflejar cambios recientes de fecha, hora o estado.
        model.addAttribute("citas", citaRepository.findByClienteIdOrderByFechaDescHoraDesc(clienteId));
        return "clientes/citas/lista";
    }
}
