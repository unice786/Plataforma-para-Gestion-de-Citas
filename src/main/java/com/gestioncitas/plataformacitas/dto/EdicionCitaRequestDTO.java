package com.gestioncitas.plataformacitas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

/** Datos que un administrador puede modificar de una cita (SCRUM-9). */
public class EdicionCitaRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotNull(message = "Debe seleccionar un servicio")
    @Positive(message = "Debe seleccionar un servicio válido")
    private Long servicioId;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }
}
