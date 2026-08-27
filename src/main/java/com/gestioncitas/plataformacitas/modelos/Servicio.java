package com.gestioncitas.plataformacitas.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicios")
@Data
@NoArgsConstructor
public class Servicio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "La categoría del servicio es obligatoria")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "categoria_id", nullable = false)
	private CategoriaServicio categoria;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
	@Column(nullable = false, length = 100)
	private String nombre;

	@Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
	@Column(length = 1000)
	private String descripcion;

	@NotNull(message = "El precio es obligatorio")
	@DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal precio;

	@NotNull(message = "La duración es obligatoria")
	@Positive(message = "La duración debe ser mayor que cero")
	@Column(name = "duracion_minutos", nullable = false)
	private Integer duracionMinutos;

	@NotNull(message = "El estado activo del servicio es obligatorio")
	@Column(nullable = false)
	private Boolean activo = true;

	@ManyToMany(mappedBy = "servicios")
	private List<Empleado> empleados = new ArrayList<>();

	@OneToMany(mappedBy = "servicio")
	private List<Cita> citas = new ArrayList<>();
}
