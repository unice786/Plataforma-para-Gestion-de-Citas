package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.ClienteResponseDTO;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar la lista de clientes registrados.
 * Expone GET /api/clientes para seleccionar el cliente en la reserva.
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteRestController {

    private final ClienteRepository clienteRepository;

    public ClienteRestController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Devuelve la lista de todos los clientes activos registrados.
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        List<ClienteResponseDTO> dtos = clientes.stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .map(c -> new ClienteResponseDTO(
                        c.getId(),
                        c.getNombre(),
                        c.getCorreo(),
                        c.getTelefono()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
