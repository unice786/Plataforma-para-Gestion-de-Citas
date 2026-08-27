package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.dto.CitaResponseDTO;
import com.gestioncitas.plataformacitas.dto.EdicionCitaRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioDisponibleDTO;
import com.gestioncitas.plataformacitas.dto.ReprogramarCitaDTO;
import com.gestioncitas.plataformacitas.dto.ReservaCitaRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    List<HorarioDisponibleDTO> consultarDisponibilidad(Long servicioId, LocalDate fecha);

    List<HorarioDisponibleDTO> consultarDisponibilidadRango(Long servicioId, LocalDate desde, LocalDate hasta);

    CitaResponseDTO reservarCita(ReservaCitaRequestDTO request);

    void editarCita(Long citaId, EdicionCitaRequestDTO request);

    void cancelarCita(Long citaId);

    CitaResponseDTO reprogramarCita(ReprogramarCitaDTO request);
}
