package com.gestioncitas.plataformacitas.controladores;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestioncitas.plataformacitas.dto.EmpleadoResponseDTO;
import com.gestioncitas.plataformacitas.dto.HorarioBulkRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioResponseDTO;
import com.gestioncitas.plataformacitas.excepciones.HorarioNoDisponibleException;
import com.gestioncitas.plataformacitas.excepciones.RecursoNoEncontradoException;
import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.servicios.EmpleadoService;
import com.gestioncitas.plataformacitas.servicios.HorarioDisponibilidadService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/horarios")
public class HorarioController {

	private final HorarioDisponibilidadService horarioService;
	private final EmpleadoService empleadoService;

	public HorarioController(HorarioDisponibilidadService horarioService, EmpleadoService empleadoService) {
		this.horarioService = horarioService;
		this.empleadoService = empleadoService;
	}

	@GetMapping
	public String listar(Model model, HttpSession session,
						 @RequestParam(required = false) Long empleadoId,
						 @RequestParam(required = false) LocalDate desde,
						 @RequestParam(required = false) LocalDate hasta) {
		if (!esAdmin(session)) return "redirect:/login";

		List<HorarioResponseDTO> horarios;
		if (empleadoId != null) {
			horarios = horarioService.listarPorEmpleado(empleadoId);
			if (desde != null || hasta != null) {
				LocalDate fDesde = desde != null ? desde : LocalDate.of(2000, 1, 1);
				LocalDate fHasta = hasta != null ? hasta : LocalDate.of(2099, 12, 31);
				horarios = horarios.stream()
						.filter(h -> !h.getFecha().isBefore(fDesde) && !h.getFecha().isAfter(fHasta))
						.toList();
			}
		} else {
			horarios = horarioService.listarPorRangoFechas(desde, hasta);
		}

		Map<String, List<HorarioResponseDTO>> agrupados = new LinkedHashMap<>();
		for (HorarioResponseDTO h : horarios) {
			String clave = h.getEmpleadoNombre() != null ? h.getEmpleadoNombre() : "Sin empleado";
			agrupados.computeIfAbsent(clave, k -> new java.util.ArrayList<>()).add(h);
		}

		model.addAttribute("horarios", horarios);
		model.addAttribute("horariosAgrupados", agrupados);
		model.addAttribute("empleados", empleadoService.listarActivos());
		model.addAttribute("empleadoFiltro", empleadoId);
		model.addAttribute("desdeFiltro", desde);
		model.addAttribute("hastaFiltro", hasta);
		model.addAttribute("bulk", new HorarioBulkRequestDTO());
		return "admin-horarios";
	}

	@GetMapping("/nuevo")
	public String mostrarFormularioNuevo(Model model, HttpSession session) {
		if (!esAdmin(session)) return "redirect:/login";
		prepararFormulario(model, new HorarioRequestDTO());
		return "admin-horario-formulario";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("horario") HorarioRequestDTO horario,
						BindingResult resultado,
						Model model,
						RedirectAttributes atributos,
						HttpSession session) {
		if (!esAdmin(session)) return "redirect:/login";
		if (resultado.hasErrors()) {
			prepararFormulario(model, horario);
			return "admin-horario-formulario";
		}

		try {
			horarioService.crear(horario);
			atributos.addFlashAttribute("exito", "Horario creado correctamente.");
			return "redirect:/admin/horarios";
		} catch (HorarioNoDisponibleException | RecursoNoEncontradoException ex) {
			model.addAttribute("error", ex.getMessage());
			prepararFormulario(model, horario);
			return "admin-horario-formulario";
		}
	}

	@PostMapping("/bulk")
	public String crearEnBloque(@Valid @ModelAttribute("bulk") HorarioBulkRequestDTO bulk,
								BindingResult resultado,
								Model model,
								RedirectAttributes atributos,
								HttpSession session) {
		if (!esAdmin(session)) return "redirect:/login";
		if (resultado.hasErrors()) {
			return "redirect:/admin/horarios";
		}

		try {
			List<HorarioResponseDTO> creados = horarioService.crearEnBloque(bulk);
			if (creados.isEmpty()) {
				atributos.addFlashAttribute("error", "No se crearon horarios. Verifica las fechas, horas y que no existan duplicados.");
			} else {
				atributos.addFlashAttribute("exito", "Se crearon " + creados.size() + " horario(s) correctamente.");
			}
		} catch (RecursoNoEncontradoException | HorarioNoDisponibleException ex) {
			atributos.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/admin/horarios";
	}

	@GetMapping("/{id}/editar")
	public String mostrarFormularioEdicion(@PathVariable Long id, Model model,
										   RedirectAttributes atributos,
										   HttpSession session) {
		if (!esAdmin(session)) return "redirect:/login";
		try {
			HorarioResponseDTO existente = horarioService.buscarPorId(id);
			HorarioRequestDTO horario = new HorarioRequestDTO();
			horario.setEmpleadoId(existente.getEmpleadoId());
			horario.setFecha(existente.getFecha());
			horario.setHoraInicio(existente.getHoraInicio());
			horario.setHoraFin(existente.getHoraFin());
			model.addAttribute("horarioId", id);
			prepararFormulario(model, horario);
			return "admin-horario-formulario";
		} catch (RecursoNoEncontradoException ex) {
			atributos.addFlashAttribute("error", "El horario solicitado no existe.");
			return "redirect:/admin/horarios";
		}
	}

	@PostMapping("/{id}/editar")
	public String editar(@PathVariable Long id,
						 @Valid @ModelAttribute("horario") HorarioRequestDTO horario,
						 BindingResult resultado,
						 Model model,
						 RedirectAttributes atributos,
						 HttpSession session) {
		if (!esAdmin(session)) return "redirect:/login";
		if (resultado.hasErrors()) {
			model.addAttribute("horarioId", id);
			prepararFormulario(model, horario);
			return "admin-horario-formulario";
		}

		try {
			horarioService.actualizar(id, horario);
			atributos.addFlashAttribute("exito", "Horario actualizado correctamente.");
			return "redirect:/admin/horarios";
		} catch (RecursoNoEncontradoException ex) {
			atributos.addFlashAttribute("error", "El horario solicitado no existe.");
			return "redirect:/admin/horarios";
		} catch (HorarioNoDisponibleException ex) {
			model.addAttribute("horarioId", id);
			model.addAttribute("error", ex.getMessage());
			prepararFormulario(model, horario);
			return "admin-horario-formulario";
		}
	}

	@PostMapping("/{id}/eliminar")
	public String eliminar(@PathVariable Long id, RedirectAttributes atributos, HttpSession session) {
		if (!esAdmin(session)) return "redirect:/login";
		try {
			horarioService.eliminar(id);
			atributos.addFlashAttribute("exito", "Horario eliminado correctamente.");
		} catch (RecursoNoEncontradoException ex) {
			atributos.addFlashAttribute("error", "El horario solicitado no existe.");
		}
		return "redirect:/admin/horarios";
	}

	private void prepararFormulario(Model model, HorarioRequestDTO horario) {
		model.addAttribute("horario", horario);
		model.addAttribute("empleados", empleadoService.listarActivos());
	}

	private boolean esAdmin(HttpSession session) {
		Usuario usuario = (Usuario) session.getAttribute("usuario");
		return usuario != null && usuario.getRol() == RolUsuario.ADMINISTRADOR;
	}
}
