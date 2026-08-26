package com.gestioncitas.plataformacitas.servicios;

import java.time.LocalDate;
import java.util.List;

import com.gestioncitas.plataformacitas.dto.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioResponseDTO;

public interface HorarioDisponibilidadService {

	HorarioResponseDTO crear(HorarioRequestDTO request);

	HorarioResponseDTO asignar(HorarioRequestDTO request);

	HorarioResponseDTO actualizar(Long id, HorarioRequestDTO request);

	void eliminar(Long id);

	HorarioResponseDTO buscarPorId(Long id);

	List<HorarioResponseDTO> listarTodos();

	List<HorarioResponseDTO> listarDisponibles(Long empleadoId, LocalDate fecha);

	List<HorarioResponseDTO> consultarDisponibilidad(Long empleadoId, LocalDate fecha);
}
