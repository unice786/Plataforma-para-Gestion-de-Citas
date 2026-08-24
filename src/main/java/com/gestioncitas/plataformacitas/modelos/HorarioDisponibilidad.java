package com.gestioncitas.plataformacitas.modelos;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "horarios_disponibilidad")
@Getter
@Setter
@NoArgsConstructor
public class HorarioDisponibilidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "El empleado del horario es obligatorio")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "empleado_id", nullable = false)
	private Empleado empleado;

	@NotNull(message = "La fecha del horario es obligatoria")
	@Column(nullable = false)
	private LocalDate fecha;

	@NotNull(message = "La hora de inicio es obligatoria")
	@Column(name = "hora_inicio", nullable = false)
	private LocalTime horaInicio;

	@NotNull(message = "La hora de fin es obligatoria")
	@Column(name = "hora_fin", nullable = false)
	private LocalTime horaFin;

	@NotBlank(message = "El estado del horario es obligatorio")
	@Column(nullable = false, length = 20)
	private String estado = "DISPONIBLE";
}
