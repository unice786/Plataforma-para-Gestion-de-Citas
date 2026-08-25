package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Empleado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Empleado}.
 */
@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    /**
     * Devuelve todos los empleados activos que ofrecen un servicio específico.
     *
     * @param servicioId ID del servicio
     * @return Lista de empleados que ofrecen ese servicio
     */
    @Query("""
            SELECT e FROM Empleado e
            JOIN e.servicios s
            WHERE s.id = :servicioId
              AND e.activo = true
            ORDER BY e.nombre ASC
            """)
    List<Empleado> findEmpleadosByServicioId(@Param("servicioId") Long servicioId);
}
