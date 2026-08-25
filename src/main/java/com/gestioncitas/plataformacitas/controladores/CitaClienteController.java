package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CitaClienteController {

	private final ClienteRepository clienteRepository;
	private final CitaRepository citaRepository;

	public CitaClienteController(ClienteRepository clienteRepository, CitaRepository citaRepository) {
		this.clienteRepository = clienteRepository;
		this.citaRepository = citaRepository;
	}

	@GetMapping("/mis-citas")
	public String listarCitas(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/login";
		}

		Cliente cliente = clienteRepository.findById(usuario.getId()).orElse(null);
		if (cliente == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
		}

		model.addAttribute("cliente", cliente);
		// Se consulta en cada carga para reflejar cambios recientes de fecha, hora o estado.
		model.addAttribute("citas", citaRepository.findByClienteIdOrderByFechaDescHoraDesc(cliente.getId()));
		return "mis-citas";
	}
}
