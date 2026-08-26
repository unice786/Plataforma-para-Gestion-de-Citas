package com.gestioncitas.plataformacitas.servicios.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestioncitas.plataformacitas.dto.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioResponseDTO;
import com.gestioncitas.plataformacitas.excepciones.HorarioNoDisponibleException;
import com.gestioncitas.plataformacitas.excepciones.RecursoNoEncontradoException;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
import com.gestioncitas.plataformacitas.servicios.HorarioDisponibilidadService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HorarioDisponibilidadServiceImpl implements HorarioDisponibilidadService {

	private static final String ESTADO_DISPONIBLE = "DISPONIBLE";

	private final HorarioDisponibilidadRepository horarioRepository;
	private final EmpleadoRepository empleadoRepository;

	@Override
	public HorarioResponseDTO crear(HorarioRequestDTO request) {
		Empleado empleado = obtenerEmpleadoActivo(request.getEmpleadoId());
		validarDuplicado(empleado.getId(), request, null);

		HorarioDisponibilidad horario = new HorarioDisponibilidad();
		horario.setEmpleado(empleado);
		horario.setFecha(request.getFecha());
		horario.setHoraInicio(request.getHoraInicio());
		horario.setHoraFin(request.getHoraFin());
		horario.setEstado(ESTADO_DISPONIBLE);

		return toResponse(horarioRepository.save(horario));
	}

	@Override
	public HorarioResponseDTO asignar(HorarioRequestDTO request) {
		return crear(request);
	}

	@Override
	public HorarioResponseDTO actualizar(Long id, HorarioRequestDTO request) {
		HorarioDisponibilidad horario = obtenerHorario(id);
		Empleado empleado = obtenerEmpleadoActivo(request.getEmpleadoId());
		validarDuplicado(empleado.getId(), request, id);

		horario.setEmpleado(empleado);
		horario.setFecha(request.getFecha());
		horario.setHoraInicio(request.getHoraInicio());
		horario.setHoraFin(request.getHoraFin());

		return toResponse(horarioRepository.save(horario));
	}

	@Override
	public void eliminar(Long id) {
		HorarioDisponibilidad horario = obtenerHorario(id);
		horarioRepository.delete(horario);
	}

	@Override
	@Transactional(readOnly = true)
	public HorarioResponseDTO buscarPorId(Long id) {
		return toResponse(obtenerHorario(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<HorarioResponseDTO> listarTodos() {
		return horarioRepository.findAllConEmpleadoOrderByFechaAsc()
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<HorarioResponseDTO> listarDisponibles(Long empleadoId, LocalDate fecha) {
		if (empleadoId != null) {
			return consultarDisponibilidad(empleadoId, fecha);
		}

		List<HorarioDisponibilidad> horarios = horarioRepository
				.findByEstadoOrderByFechaAscHoraInicioAsc(ESTADO_DISPONIBLE);

		if (fecha != null) {
			return horarios.stream()
					.filter(h -> fecha.equals(h.getFecha()))
					.map(this::toResponse)
					.toList();
		}

		return horarios.stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<HorarioResponseDTO> consultarDisponibilidad(Long empleadoId, LocalDate fecha) {
		if (!empleadoRepository.existsById(empleadoId)) {
			throw new RecursoNoEncontradoException("Empleado", empleadoId);
		}

		List<HorarioDisponibilidad> horarios = fecha == null
				? horarioRepository.findByEmpleadoIdAndEstado(empleadoId, ESTADO_DISPONIBLE)
				: horarioRepository.findByEmpleadoIdAndFechaAndEstado(empleadoId, fecha, ESTADO_DISPONIBLE);

		return horarios.stream().map(this::toResponse).toList();
	}

	private void validarDuplicado(Long empleadoId, HorarioRequestDTO request, Long horarioId) {
		boolean duplicado = horarioId == null
				? horarioRepository.existsByEmpleadoIdAndFechaAndHoraInicioAndHoraFin(
						empleadoId, request.getFecha(), request.getHoraInicio(), request.getHoraFin())
				: horarioRepository.existsByEmpleadoIdAndFechaAndHoraInicioAndHoraFinAndIdNot(
						empleadoId, request.getFecha(), request.getHoraInicio(), request.getHoraFin(), horarioId);

		if (duplicado) {
			throw new HorarioNoDisponibleException(
					"Ya existe un horario para este empleado en la misma fecha y hora.");
		}
	}

	private Empleado obtenerEmpleadoActivo(Long empleadoId) {
		if (empleadoId == null) {
			throw new RecursoNoEncontradoException("Debes seleccionar un empleado.");
		}

		Empleado empleado = empleadoRepository.findById(empleadoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("Empleado", empleadoId));

		if (!Boolean.TRUE.equals(empleado.getActivo())) {
			throw new HorarioNoDisponibleException("No se pueden asignar horarios a un empleado inactivo.");
		}

		return empleado;
	}

	private HorarioDisponibilidad obtenerHorario(Long id) {
		return horarioRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Horario", id));
	}

	private HorarioResponseDTO toResponse(HorarioDisponibilidad horario) {
		Long empleadoId = horario.getEmpleado() != null ? horario.getEmpleado().getId() : null;
		String empleadoNombre = horario.getEmpleado() != null ? horario.getEmpleado().getNombre() : null;

		return new HorarioResponseDTO(
				horario.getId(),
				empleadoId,
				empleadoNombre,
				horario.getFecha(),
				horario.getHoraInicio(),
				horario.getHoraFin(),
				horario.getEstado());
	}
}
