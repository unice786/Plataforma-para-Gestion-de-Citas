package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.RolUsuario;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByTokenVerificacion(String token);
    Optional<Usuario> findByTokenRecuperacion(String token);
    boolean existsByCorreo(String correo);
    List<Usuario> findByRol(RolUsuario rol);
}