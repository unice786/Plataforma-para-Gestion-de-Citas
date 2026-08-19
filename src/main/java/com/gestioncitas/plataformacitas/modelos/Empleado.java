package com.gestioncitas.plataformacitas.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empleados")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Empleado extends Usuario {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;
    @ManyToMany
    @JoinTable(name = "empleado_servicio", joinColumns = @JoinColumn(name = "empleado_id"), inverseJoinColumns = @JoinColumn(name = "servicio_id"))
    private List<Servicio> servicios = new ArrayList<>();
    @OneToMany(mappedBy = "empleado")
    private List<HorarioDisponibilidad> horarios = new ArrayList<>();
    @OneToMany(mappedBy = "empleado")
    private List<Cita> citas = new ArrayList<>();
    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
    public List<Servicio> getServicios() { return servicios; }
    public void setServicios(List<Servicio> servicios) { this.servicios = servicios; }
    public List<HorarioDisponibilidad> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioDisponibilidad> horarios) { this.horarios = horarios; }
    public List<Cita> getCitas() { return citas; }
    public void setCitas(List<Cita> citas) { this.citas = citas; }
}
