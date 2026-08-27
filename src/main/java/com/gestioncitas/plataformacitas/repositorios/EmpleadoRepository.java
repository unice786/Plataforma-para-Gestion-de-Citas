package com.gestioncitas.plataformacitas.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestioncitas.plataformacitas.modelos.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

	boolean existsByCorreo(String correo);

	boolean existsByCorreoAndIdNot(String correo, Long id);

	List<Empleado> findByActivoTrue();

	@Query("""
			SELECT e FROM Empleado e JOIN e.servicios s
			WHERE s.id = :servicioId AND e.activo = true ORDER BY e.nombre ASC
			""")
	List<Empleado> findEmpleadosByServicioId(@Param("servicioId") Long servicioId);
}
