package com.gestioncitas.plataformacitas.modelos;

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

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias_servicio")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaServicio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre de la categoría es obligatorio")
	@Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
	@Column(nullable = false, length = 50)
	private String nombre;

	@Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
	@Column(length = 255)
	private String descripcion;

	@OneToMany(mappedBy = "categoria")
	private List<Servicio> servicios = new ArrayList<>();
}
