package com.gestioncitas.plataformacitas.servicios.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestioncitas.plataformacitas.dtos.EmpleadoRequestDTO;
import com.gestioncitas.plataformacitas.dtos.EmpleadoResponseDTO;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Especialidad;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.EspecialidadRepository;
import com.gestioncitas.plataformacitas.servicios.EmpleadoService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpleadoServiceImpl implements EmpleadoService {

	private final EmpleadoRepository empleadoRepository;
	private final EspecialidadRepository especialidadRepository;

	@Override
	public EmpleadoResponseDTO crear(EmpleadoRequestDTO request) {
		if (empleadoRepository.existsByCorreo(request.getCorreo())) {
			throw new IllegalArgumentException("Ya existe un empleado con ese correo");
		}

		Empleado empleado = new Empleado();
		empleado.setActivo(true);
		aplicarDatos(empleado, request);
		return toResponse(empleadoRepository.save(empleado));
	}

	@Override
	public EmpleadoResponseDTO actualizar(Long id, EmpleadoRequestDTO request) {
		Empleado empleado = obtenerEmpleado(id);
		if (empleadoRepository.existsByCorreoAndIdNot(request.getCorreo(), id)) {
			throw new IllegalArgumentException("Ya existe un empleado con ese correo");
		}

		aplicarDatos(empleado, request);
		return toResponse(empleadoRepository.save(empleado));
	}

	@Override
	public EmpleadoResponseDTO darDeBaja(Long id) {
		Empleado empleado = obtenerEmpleado(id);
		empleado.setActivo(false);
		return toResponse(empleadoRepository.save(empleado));
	}

	@Override
	@Transactional(readOnly = true)
	public List<EmpleadoResponseDTO> listarActivos() {
		return empleadoRepository.findByActivoTrue()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private void aplicarDatos(Empleado empleado, EmpleadoRequestDTO request) {
		Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
				.orElseThrow(() -> new EntityNotFoundException(
						"No se encontró la especialidad con id " + request.getEspecialidadId()));

		empleado.setNombre(request.getNombre());
		empleado.setCorreo(request.getCorreo());
		empleado.setPassword(request.getPassword());
		empleado.setEspecialidad(especialidad);
	}

	private Empleado obtenerEmpleado(Long id) {
		return empleadoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("No se encontró el empleado con id " + id));
	}

	private EmpleadoResponseDTO toResponse(Empleado empleado) {
		String nombreEspecialidad = empleado.getEspecialidad() != null
				? empleado.getEspecialidad().getNombre()
				: null;

		return new EmpleadoResponseDTO(
				empleado.getId(),
				empleado.getNombre(),
				empleado.getCorreo(),
				null,
				nombreEspecialidad,
				empleado.getActivo());
	}
}
