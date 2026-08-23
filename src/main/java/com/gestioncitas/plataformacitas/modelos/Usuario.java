package com.gestioncitas.plataformacitas.modelos;

  import java.time.LocalDateTime;

  import jakarta.persistence.Column;
  import jakarta.persistence.Entity;
  import jakarta.persistence.GeneratedValue;
  import jakarta.persistence.GenerationType;
  import jakarta.persistence.Id;
  import jakarta.persistence.Inheritance;
  import jakarta.persistence.InheritanceType;
  import jakarta.persistence.Table;
  import jakarta.persistence.Transient;
  import lombok.Data;

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

      @Column(nullable = false, unique = true, length = 150)
      private String correo;

      @Column(name = "password", nullable = false, length = 255)
      private String password;

      @Column(nullable = false)
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

      @Transient
      public String getContrasena() {
          return password;
      }

      public void setContrasena(String contrasena) {
          this.password = contrasena;
      }
  }