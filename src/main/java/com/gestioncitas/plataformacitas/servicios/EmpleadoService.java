package com.gestioncitas.plataformacitas.servicios;

import java.util.List;

import com.gestioncitas.plataformacitas.dto.EmpleadoRequestDTO;
import com.gestioncitas.plataformacitas.dto.EmpleadoResponseDTO;

public interface EmpleadoService {

	EmpleadoResponseDTO crear(EmpleadoRequestDTO request);

	EmpleadoResponseDTO actualizar(Long id, EmpleadoRequestDTO request);

	EmpleadoResponseDTO darDeBaja(Long id);

	List<EmpleadoResponseDTO> listarActivos();
}
