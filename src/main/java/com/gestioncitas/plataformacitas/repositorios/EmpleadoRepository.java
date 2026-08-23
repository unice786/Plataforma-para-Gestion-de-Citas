package com.gestioncitas.plataformacitas.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestioncitas.plataformacitas.modelos.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

	boolean existsByCorreo(String correo);

	boolean existsByCorreoAndIdNot(String correo, Long id);

	List<Empleado> findByActivoTrue();
}
