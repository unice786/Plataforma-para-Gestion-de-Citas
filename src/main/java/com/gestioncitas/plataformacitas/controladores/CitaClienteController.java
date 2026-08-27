package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CitaClienteController {

	private static final List<EstadoCita> ESTADOS_REPROGRAMABLES = List.of(
			EstadoCita.PENDIENTE,
			EstadoCita.CONFIRMADA
	);

	private final ClienteRepository clienteRepository;
	private final CitaRepository citaRepository;

	public CitaClienteController(ClienteRepository clienteRepository, CitaRepository citaRepository) {
		this.clienteRepository = clienteRepository;
		this.citaRepository = citaRepository;
	}

	@GetMapping("/mis-citas")
	public String listarCitas(HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null || usuario.getRol() != RolUsuario.CLIENTE) {
			return "redirect:/login";
		}

		Cliente cliente = clienteRepository.findById(usuario.getId()).orElse(null);
		if (cliente == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
		}

		model.addAttribute("cliente", cliente);
		model.addAttribute("citas", citaRepository.findByClienteIdOrderByFechaDescHoraDesc(cliente.getId()));
		return "mis-citas";
	}

	@GetMapping("/reprogramar/{id}")
	public String reprogramarCita(@PathVariable("id") Long id, HttpSession session, Model model) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null || usuario.getRol() != RolUsuario.CLIENTE) {
			return "redirect:/login";
		}

		Cliente cliente = clienteRepository.findById(usuario.getId()).orElse(null);
		if (cliente == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
		}

		Optional<Cita> citaOpt = citaRepository.findByIdAndClienteId(id, cliente.getId());
		if (citaOpt.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");
		}

		Cita cita = citaOpt.get();
		if (!ESTADOS_REPROGRAMABLES.contains(cita.getEstado())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"La cita no puede ser reprogramada porque su estado es " + cita.getEstado());
		}

		model.addAttribute("cita", cita);
		model.addAttribute("clienteId", cliente.getId());
		return "reprogramar-cita";
	}
}
