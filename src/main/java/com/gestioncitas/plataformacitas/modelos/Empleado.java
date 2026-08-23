 package com.gestioncitas.plataformacitas.modelos;

  import java.util.ArrayList;
  import java.util.List;

  import jakarta.persistence.CascadeType;
  import jakarta.persistence.Entity;
  import jakarta.persistence.FetchType;
  import jakarta.persistence.JoinColumn;
  import jakarta.persistence.JoinTable;
  import jakarta.persistence.ManyToMany;
  import jakarta.persistence.ManyToOne;
  import jakarta.persistence.OneToMany;
  import jakarta.persistence.PrimaryKeyJoinColumn;
  import jakarta.persistence.Table;
  import lombok.Getter;
  import lombok.NoArgsConstructor;
  import lombok.Setter;

  @Entity
  @Table(name = "empleados")
  @PrimaryKeyJoinColumn(name = "usuario_id")
  @Getter
  @Setter
  @NoArgsConstructor
  public class Empleado extends Usuario {

      @ManyToOne(optional = false, fetch = FetchType.LAZY)
      @JoinColumn(name = "especialidad_id", nullable = false)
      private Especialidad especialidad;

      @ManyToMany
      @JoinTable(
          name = "empleado_servicio",
          joinColumns = @JoinColumn(name = "empleado_id"),
          inverseJoinColumns = @JoinColumn(name = "servicio_id")
      )
      private List<Servicio> servicios = new ArrayList<>();

      @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
      private List<HorarioDisponibilidad> horarios = new ArrayList<>();

      @OneToMany(mappedBy = "empleado")
      private List<Cita> citas = new ArrayList<>();
  }

