package com.gestioncitas.plataformacitas.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Data
public class Cliente extends Usuario {

    private String telefono;

    public Cliente() {
    }

}