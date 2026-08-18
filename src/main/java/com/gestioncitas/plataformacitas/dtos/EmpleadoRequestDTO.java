package com.gestioncitas.plataformacitas.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmpleadoRequestDTO {

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre no puede superar 100 caracteres")
	private String nombre;

	@NotBlank(message = "El correo es obligatorio")
	@Email(message = "El correo no tiene un formato válido")
	@Size(max = 150, message = "El correo no puede superar 150 caracteres")
	private String correo;

	@NotBlank(message = "La contraseña es obligatoria")
	@Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
	private String password;

	@NotBlank(message = "El teléfono es obligatorio")
	@Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
	private String telefono;

	@NotNull(message = "La especialidad es obligatoria")
	private Long especialidadId;
}
