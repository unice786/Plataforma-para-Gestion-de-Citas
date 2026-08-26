package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CitaClienteController {

	private final ClienteRepository clienteRepository;

	public CitaClienteController(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	@GetMapping("/mis-citas")
	public String listarCitas(
			@SessionAttribute(name = "usuario", required = false) Usuario usuario,
			Model model) {
		if (usuario == null) {
			return "redirect:/login";
		}
		if (!(usuario instanceof Cliente)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso exclusivo para clientes");
		}

		Cliente cliente = clienteRepository.findById(usuario.getId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.FORBIDDEN, "Cliente no encontrado"));

		model.addAttribute("cliente", cliente);
		return "mis-citas";
	}
}
