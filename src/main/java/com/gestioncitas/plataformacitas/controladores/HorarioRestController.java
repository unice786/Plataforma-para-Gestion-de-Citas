package com.gestioncitas.plataformacitas.controladores;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestioncitas.plataformacitas.dto.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioResponseDTO;
import com.gestioncitas.plataformacitas.servicios.HorarioDisponibilidadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horarios")
public class HorarioRestController {

	private final HorarioDisponibilidadService horarioService;

	public HorarioRestController(HorarioDisponibilidadService horarioService) {
		this.horarioService = horarioService;
	}

	@GetMapping("/disponibles")
	public ResponseEntity<List<HorarioResponseDTO>> listarDisponibles(
			@RequestParam(value = "empleadoId", required = false) Long empleadoId,
			@RequestParam(value = "fecha", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return ResponseEntity.ok(horarioService.listarDisponibles(empleadoId, fecha));
	}

	@PostMapping
	public ResponseEntity<HorarioResponseDTO> crear(@Valid @RequestBody HorarioRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.crear(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<HorarioResponseDTO> actualizar(
			@PathVariable Long id,
			@Valid @RequestBody HorarioRequestDTO request) {
		return ResponseEntity.ok(horarioService.actualizar(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		horarioService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
