package com.gestioncitas.plataformacitas.servicios.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestioncitas.plataformacitas.dtos.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dtos.HorarioResponseDTO;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
import com.gestioncitas.plataformacitas.servicios.HorarioDisponibilidadService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HorarioDisponibilidadServiceImpl implements HorarioDisponibilidadService {

	private static final String ESTADO_DISPONIBLE = "DISPONIBLE";

	private final HorarioDisponibilidadRepository horarioRepository;
	private final EmpleadoRepository empleadoRepository;

	@Override
	public HorarioResponseDTO asignar(HorarioRequestDTO request) {
		Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
				.orElseThrow(() -> new EntityNotFoundException(
						"No se encontró el empleado con id " + request.getEmpleadoId()));

		if (!Boolean.TRUE.equals(empleado.getActivo())) {
			throw new IllegalStateException("No se pueden asignar horarios a un empleado inactivo");
		}

		HorarioDisponibilidad horario = new HorarioDisponibilidad();
		horario.setEmpleado(empleado);
		horario.setFecha(request.getFecha());
		horario.setHoraInicio(request.getHoraInicio());
		horario.setHoraFin(request.getHoraFin());
		horario.setEstado(ESTADO_DISPONIBLE);

		return toResponse(horarioRepository.save(horario));
	}

	@Override
	@Transactional(readOnly = true)
	public List<HorarioResponseDTO> consultarDisponibilidad(Long empleadoId, LocalDate fecha) {
		if (!empleadoRepository.existsById(empleadoId)) {
			throw new EntityNotFoundException("No se encontró el empleado con id " + empleadoId);
		}

		List<HorarioDisponibilidad> horarios = fecha == null
				? horarioRepository.findByEmpleadoIdAndEstado(empleadoId, ESTADO_DISPONIBLE)
				: horarioRepository.findByEmpleadoIdAndFechaAndEstado(empleadoId, fecha, ESTADO_DISPONIBLE);

		return horarios.stream().map(this::toResponse).toList();
	}

	private HorarioResponseDTO toResponse(HorarioDisponibilidad horario) {
		return new HorarioResponseDTO(
				horario.getId(),
				horario.getFecha(),
				horario.getHoraInicio(),
				horario.getHoraFin(),
				horario.getEstado());
	}
}
