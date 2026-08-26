package com.gestioncitas.plataformacitas.dto;

import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import java.time.LocalDate;
import java.time.LocalTime;

/** Datos necesarios para mostrar una cita en la pantalla del cliente. */
public class CitaClienteResponseDTO {

    private final Long id;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String servicioNombre;
    private final EstadoCita estado;

    public CitaClienteResponseDTO(Long id, LocalDate fecha, LocalTime hora,
                                  String servicioNombre, EstadoCita estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.servicioNombre = servicioNombre;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public EstadoCita getEstado() {
        return estado;
    }
}
