package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    @EntityGraph(attributePaths = {"servicio", "empleado"})
    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);

    @EntityGraph(attributePaths = {"servicio", "empleado"})
    Optional<Cita> findByIdAndClienteId(Long id, Long clienteId);

    @EntityGraph(attributePaths = {"servicio", "cliente"})
    List<Cita> findByEmpleadoIdOrderByFechaDescHoraDesc(Long empleadoId);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.servicio
            WHERE c.empleado.id = :empleadoId
              AND c.fecha = :fecha
              AND c.estado IN :estados
            ORDER BY c.hora ASC
            """)
    List<Cita> findCitasActivasByEmpleadoAndFecha(
            @Param("empleadoId") Long empleadoId,
            @Param("fecha") LocalDate fecha,
            @Param("estados") List<EstadoCita> estados
    );

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.servicio
            WHERE c.empleado.id = :empleadoId
              AND c.fecha = :fecha
              AND c.estado IN :estados
              AND c.id <> :citaId
            ORDER BY c.hora ASC
            """)
    List<Cita> findCitasActivasByEmpleadoAndFechaExcludingId(
            @Param("empleadoId") Long empleadoId,
            @Param("fecha") LocalDate fecha,
            @Param("estados") List<EstadoCita> estados,
            @Param("citaId") Long citaId
    );

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.servicio
            JOIN FETCH c.cliente
            LEFT JOIN FETCH c.empleado
            WHERE (:fecha IS NULL OR c.fecha = :fecha)
              AND (:cliente IS NULL OR LOWER(c.cliente.nombre) LIKE LOWER(CONCAT('%', :cliente, '%')))
            ORDER BY c.fecha DESC, c.hora DESC
            """)
    List<Cita> buscarParaAdministrador(
            @Param("fecha") LocalDate fecha,
            @Param("cliente") String cliente
    );

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.servicio
            JOIN FETCH c.cliente
            LEFT JOIN FETCH c.empleado
            WHERE c.id = :id
            """)
    Optional<Cita> buscarPorIdParaAdministrador(@Param("id") Long id);
}
