package com.gestioncitas.plataformacitas.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponseDTO {

	private Long id;
	private String nombre;
	private String correo;
	private String telefono;
	private String nombreEspecialidad;
	private Boolean activo;
}
