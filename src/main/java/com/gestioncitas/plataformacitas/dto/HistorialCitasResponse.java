package com.gestioncitas.plataformacitas.dto;

import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import java.time.LocalDate;
import java.time.LocalTime;

public class HistorialCitasResponse {

    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String nombreServicio;
    private String nombreEmpleado;
    private String estado;

    public HistorialCitasResponse() {
    }

    public HistorialCitasResponse(Long id, LocalDate fecha, LocalTime hora, String nombreServicio, String nombreEmpleado, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.nombreServicio = nombreServicio;
        this.nombreEmpleado = nombreEmpleado;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}