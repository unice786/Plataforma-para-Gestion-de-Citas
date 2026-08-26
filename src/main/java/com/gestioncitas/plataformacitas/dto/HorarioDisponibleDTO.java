package com.gestioncitas.plataformacitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class HorarioDisponibleDTO {

    private Long empleadoId;
    private String empleadoNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public HorarioDisponibleDTO() {}

    public HorarioDisponibleDTO(Long empleadoId, String empleadoNombre,
                                LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.empleadoId = empleadoId;
        this.empleadoNombre = empleadoNombre;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    public String getEmpleadoNombre() { return empleadoNombre; }
    public void setEmpleadoNombre(String empleadoNombre) { this.empleadoNombre = empleadoNombre; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
}
