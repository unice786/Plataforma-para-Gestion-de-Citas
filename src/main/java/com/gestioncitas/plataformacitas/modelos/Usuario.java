package com.gestioncitas.plataformacitas.modelos;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    private Boolean activo = true;

    private Boolean verificado = false;

    @Column(name = "token_verificacion")
    private String tokenVerificacion;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiracion;

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    @Column(name = "token_recuperacion_expiracion")
    private LocalDateTime tokenRecuperacionExpiracion;
}