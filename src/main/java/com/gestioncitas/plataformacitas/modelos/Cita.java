package com.gestioncitas.plataformacitas.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor
public class Cita {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "El cliente de la cita es obligatorio")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@NotNull(message = "El empleado de la cita es obligatorio")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "empleado_id", nullable = false)
	private Empleado empleado;

	@NotNull(message = "El servicio de la cita es obligatorio")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "servicio_id", nullable = false)
	private Servicio servicio;

	@NotNull(message = "La fecha de la cita es obligatoria")
	@Column(nullable = false)
	private LocalDate fecha;

	@NotNull(message = "La hora de la cita es obligatoria")
	@Column(nullable = false)
	private LocalTime hora;

	@NotNull(message = "El estado de la cita es obligatorio")
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private EstadoCita estado = EstadoCita.PENDIENTE;

	@NotNull(message = "La fecha de registro es obligatoria")
	@Column(name = "fecha_registro", nullable = false)
	private LocalDateTime fechaRegistro = LocalDateTime.now();

	@Column(name = "fecha_ultima_modificacion")
	private LocalDateTime fechaUltimaModificacion;

	@Column(name = "detalle_ultimo_cambio", length = 500)
	private String detalleUltimoCambio;
}
