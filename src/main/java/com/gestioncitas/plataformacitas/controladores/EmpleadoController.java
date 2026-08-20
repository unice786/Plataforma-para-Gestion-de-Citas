package com.gestioncitas.plataformacitas.controladores;

import java.time.LocalDate;
import java.util.List;

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

import com.gestioncitas.plataformacitas.dto.EmpleadoRequestDTO;
import com.gestioncitas.plataformacitas.dto.EmpleadoResponseDTO;
import com.gestioncitas.plataformacitas.dto.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioResponseDTO;
import com.gestioncitas.plataformacitas.servicios.EmpleadoService;
import com.gestioncitas.plataformacitas.servicios.HorarioDisponibilidadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

	private final EmpleadoService empleadoService;
	private final HorarioDisponibilidadService horarioDisponibilidadService;

	@PostMapping
	public ResponseEntity<EmpleadoResponseDTO> crear(@Valid @RequestBody EmpleadoRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(empleadoService.crear(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<EmpleadoResponseDTO> actualizar(
			@PathVariable Long id,
			@Valid @RequestBody EmpleadoRequestDTO request) {
		return ResponseEntity.ok(empleadoService.actualizar(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<EmpleadoResponseDTO> darDeBaja(@PathVariable Long id) {
		return ResponseEntity.ok(empleadoService.darDeBaja(id));
	}

	@GetMapping
	public ResponseEntity<List<EmpleadoResponseDTO>> listarActivos() {
		return ResponseEntity.ok(empleadoService.listarActivos());
	}

	@PostMapping("/{id}/horarios")
	public ResponseEntity<HorarioResponseDTO> asignarHorario(
			@PathVariable Long id,
			@Valid @RequestBody HorarioRequestDTO request) {
		request.setEmpleadoId(id);
		return ResponseEntity.status(HttpStatus.CREATED).body(horarioDisponibilidadService.asignar(request));
	}

	@GetMapping("/{id}/horarios")
	public ResponseEntity<List<HorarioResponseDTO>> consultarDisponibilidad(
			@PathVariable Long id,
			@RequestParam(required = false) LocalDate fecha) {
		return ResponseEntity.ok(horarioDisponibilidadService.consultarDisponibilidad(id, fecha));
	}
}
