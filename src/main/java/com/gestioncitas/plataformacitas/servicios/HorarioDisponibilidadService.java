package com.gestioncitas.plataformacitas.servicios;

import java.time.LocalDate;
import java.util.List;

import com.gestioncitas.plataformacitas.dtos.HorarioRequestDTO;
import com.gestioncitas.plataformacitas.dtos.HorarioResponseDTO;

public interface HorarioDisponibilidadService {

	HorarioResponseDTO asignar(HorarioRequestDTO request);

	List<HorarioResponseDTO> consultarDisponibilidad(Long empleadoId, LocalDate fecha);
}
