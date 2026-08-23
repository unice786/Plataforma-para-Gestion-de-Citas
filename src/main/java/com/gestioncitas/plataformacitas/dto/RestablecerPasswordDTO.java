package com.gestioncitas.plataformacitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestablecerPasswordDTO {

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 60, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Confirma tu nueva contraseña")
    private String confirmarPassword;

    private String token;

    public RestablecerPasswordDTO() {}

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}