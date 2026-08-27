package com.gestioncitas.plataformacitas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReprogramarCitaDTO {

    @NotNull(message = "El ID de la cita es obligatorio")
    @Positive(message = "El ID de la cita debe ser válido")
    private Long citaId;

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser válido")
    private Long clienteId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    public Long getCitaId() { return citaId; }
    public void setCitaId(Long citaId) { this.citaId = citaId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}
