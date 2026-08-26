package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.EdicionCitaClienteRequestDTO;
import com.gestioncitas.plataformacitas.excepciones.HorarioNoDisponibleException;
import com.gestioncitas.plataformacitas.excepciones.RecursoNoEncontradoException;
import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.servicios.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CitaClienteController {

	private final ClienteRepository clienteRepository;
	private final CitaService citaService;

	public CitaClienteController(ClienteRepository clienteRepository, CitaService citaService) {
		this.clienteRepository = clienteRepository;
		this.citaService = citaService;
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

	@GetMapping("/mis-citas/{id}/editar")
	public String mostrarEdicion(
			@PathVariable Long id,
			@SessionAttribute(name = "usuario", required = false) Usuario usuario,
			Model model) {
		if (usuario == null) {
			return "redirect:/login";
		}
		Cliente cliente = requerirCliente(usuario);
		Cita cita = buscarCitaPropia(id, cliente.getId());
		if (cita.getEstado() != com.gestioncitas.plataformacitas.modelos.EstadoCita.PENDIENTE
				&& cita.getEstado() != com.gestioncitas.plataformacitas.modelos.EstadoCita.CONFIRMADA) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Solo puedes editar citas pendientes o confirmadas");
		}

		EdicionCitaClienteRequestDTO edicion = new EdicionCitaClienteRequestDTO();
		edicion.setFecha(cita.getFecha());
		edicion.setHora(cita.getHora());
		cargarFormulario(model, cita, edicion);
		return "editar-cita-cliente";
	}

	@PostMapping("/mis-citas/{id}/editar")
	public String editar(
			@PathVariable Long id,
			@Valid @ModelAttribute("edicionCita") EdicionCitaClienteRequestDTO edicion,
			BindingResult resultado,
			@SessionAttribute(name = "usuario", required = false) Usuario usuario,
			Model model,
			RedirectAttributes redirect) {
		if (usuario == null) {
			return "redirect:/login";
		}
		Cliente cliente = requerirCliente(usuario);
		Cita cita = buscarCitaPropia(id, cliente.getId());

		if (resultado.hasErrors()) {
			cargarFormulario(model, cita, edicion);
			return "editar-cita-cliente";
		}

		try {
			citaService.editarCitaDelCliente(id, cliente.getId(), edicion);
			redirect.addFlashAttribute("exito", "La cita fue actualizada correctamente.");
			return "redirect:/mis-citas";
		} catch (HorarioNoDisponibleException | IllegalArgumentException | IllegalStateException ex) {
			resultado.reject("edicion", ex.getMessage());
			cargarFormulario(model, cita, edicion);
			return "editar-cita-cliente";
		}
	}

	@PostMapping("/mis-citas/{id}/eliminar")
	public String eliminar(
			@PathVariable Long id,
			@SessionAttribute(name = "usuario", required = false) Usuario usuario,
			RedirectAttributes redirect) {
		if (usuario == null) {
			return "redirect:/login";
		}
		Cliente cliente = requerirCliente(usuario);
		try {
			citaService.eliminarCitaDelCliente(id, cliente.getId());
			redirect.addFlashAttribute("exito", "La cita fue eliminada correctamente.");
			return "redirect:/mis-citas";
		} catch (RecursoNoEncontradoException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");
		}
	}

	private Cliente requerirCliente(Usuario usuario) {
		if (!(usuario instanceof Cliente)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso exclusivo para clientes");
		}
		return clienteRepository.findById(usuario.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente no encontrado"));
	}

	private Cita buscarCitaPropia(Long citaId, Long clienteId) {
		try {
			return citaService.obtenerCitaDelCliente(citaId, clienteId);
		} catch (RecursoNoEncontradoException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada");
		}
	}

	private void cargarFormulario(Model model, Cita cita, EdicionCitaClienteRequestDTO edicion) {
		model.addAttribute("cita", cita);
		model.addAttribute("edicionCita", edicion);
	}
}
