package com.gestioncitas.plataformacitas.dto;

import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

<<<<<<< HEAD
/**
 * DTO de respuesta devuelto tras reservar o consultar una cita (SCRUM-1).
 */
=======
>>>>>>> origin/develop
public class CitaResponseDTO {

    private Long id;
    private String clienteNombre;
    private String empleadoNombre;
    private String servicioNombre;
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoCita estado;
    private LocalDateTime fechaRegistro;
    private String mensaje;

<<<<<<< HEAD
    // ── Constructor vacío ──────────────────────────────────────────────────

    public CitaResponseDTO() {}

    // ── Constructor completo ───────────────────────────────────────────────

=======
    public CitaResponseDTO() {}

>>>>>>> origin/develop
    public CitaResponseDTO(Long id, String clienteNombre, String empleadoNombre,
                           String servicioNombre, LocalDate fecha, LocalTime hora,
                           EstadoCita estado, LocalDateTime fechaRegistro, String mensaje) {
        this.id = id;
        this.clienteNombre = clienteNombre;
        this.empleadoNombre = empleadoNombre;
        this.servicioNombre = servicioNombre;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
        this.mensaje = mensaje;
    }

<<<<<<< HEAD
    // ── Getters y Setters ──────────────────────────────────────────────────

=======
>>>>>>> origin/develop
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getEmpleadoNombre() { return empleadoNombre; }
    public void setEmpleadoNombre(String empleadoNombre) { this.empleadoNombre = empleadoNombre; }

    public String getServicioNombre() { return servicioNombre; }
    public void setServicioNombre(String servicioNombre) { this.servicioNombre = servicioNombre; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
