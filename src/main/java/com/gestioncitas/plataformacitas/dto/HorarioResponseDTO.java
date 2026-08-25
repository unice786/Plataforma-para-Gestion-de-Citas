package com.gestioncitas.plataformacitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioResponseDTO {

	private Long id;
	private LocalDate fecha;
	private LocalTime horaInicio;
	private LocalTime horaFin;
	private String estado;
}
