package com.gestioncitas.plataformacitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HorarioRequestDTO {

	@NotNull(message = "El empleado es obligatorio")
	@Positive(message = "El ID del empleado debe ser válido")
	private Long empleadoId;

	@NotNull(message = "La fecha es obligatoria")
	private LocalDate fecha;

	@NotNull(message = "La hora de inicio es obligatoria")
	private LocalTime horaInicio;

	@NotNull(message = "La hora de fin es obligatoria")
	private LocalTime horaFin;

	@AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
	public boolean isRangoHorarioValido() {
		if (horaInicio == null || horaFin == null) {
			return true;
		}
		return horaFin.isAfter(horaInicio);
	}
}
