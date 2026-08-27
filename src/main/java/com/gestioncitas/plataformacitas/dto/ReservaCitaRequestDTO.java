package com.gestioncitas.plataformacitas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaCitaRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser válido")
    private Long clienteId;

    @NotNull(message = "El ID del empleado es obligatorio")
    @Positive(message = "El ID del empleado debe ser válido")
    private Long empleadoId;

    @NotNull(message = "El ID del servicio es obligatorio")
    @Positive(message = "El ID del servicio debe ser válido")
    private Long servicioId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    public Long getServicioId() { return servicioId; }
    public void setServicioId(Long servicioId) { this.servicioId = servicioId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}
