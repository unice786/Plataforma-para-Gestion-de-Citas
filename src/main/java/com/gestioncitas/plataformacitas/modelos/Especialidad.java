package com.gestioncitas.plataformacitas.modelos;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "especialidades")
@Getter
@Setter
@NoArgsConstructor
public class Especialidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre de la especialidad es obligatorio")
	@Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
	@Column(nullable = false, length = 100)
	private String nombre;

	@Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
	@Column(length = 255)
	private String descripcion;

	@OneToMany(mappedBy = "especialidad")
	private List<Empleado> empleados = new ArrayList<>();
}
