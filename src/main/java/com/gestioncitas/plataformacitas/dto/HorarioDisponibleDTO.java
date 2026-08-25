package com.gestioncitas.plataformacitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

<<<<<<< HEAD
/**
 * DTO que representa un bloque de horario libre disponible para reservar (SCRUM-1).
 * Devuelto por el endpoint GET /api/citas/disponibilidad.
 */
=======
>>>>>>> origin/develop
public class HorarioDisponibleDTO {

    private Long empleadoId;
    private String empleadoNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

<<<<<<< HEAD
    // ── Constructor vacío ──────────────────────────────────────────────────

    public HorarioDisponibleDTO() {}

    // ── Constructor completo ───────────────────────────────────────────────

=======
    public HorarioDisponibleDTO() {}

>>>>>>> origin/develop
    public HorarioDisponibleDTO(Long empleadoId, String empleadoNombre,
                                LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.empleadoId = empleadoId;
        this.empleadoNombre = empleadoNombre;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

<<<<<<< HEAD
    // ── Getters y Setters ──────────────────────────────────────────────────

=======
>>>>>>> origin/develop
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
