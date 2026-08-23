package com.gestioncitas.plataformacitas.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Cliente extends Usuario {

 @Column(length = 20)
      private String telefono;

      @OneToMany(mappedBy = "cliente")
      private List<Cita> citas = new ArrayList<>();

      public Cliente() {}

      public String getTelefono() {
          return telefono;
      }

      public void setTelefono(String telefono) {
          this.telefono = telefono;
      }

      public List<Cita> getCitas() {
          return citas;
      }

      public void setCitas(List<Cita> citas) {
          this.citas = citas;
      }
  }
