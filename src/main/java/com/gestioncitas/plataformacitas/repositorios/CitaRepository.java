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

    @EntityGraph(attributePaths = "servicio")
    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);

    @EntityGraph(attributePaths = "servicio")
    List<Cita> findByClienteIdOrderByFechaAscHoraAsc(Long clienteId);

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
            @Param("estados") List<EstadoCita> estados);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.cliente
            JOIN FETCH c.servicio
            WHERE (:fecha IS NULL OR c.fecha = :fecha)
              AND (:cliente IS NULL
                   OR LOWER(c.cliente.nombre) LIKE LOWER(CONCAT('%', :cliente, '%')))
            ORDER BY c.fecha DESC, c.hora DESC
            """)
    List<Cita> buscarParaAdministrador(
            @Param("fecha") LocalDate fecha,
            @Param("cliente") String cliente);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.cliente
            JOIN FETCH c.empleado
            JOIN FETCH c.servicio
            WHERE c.id = :id
            """)
    java.util.Optional<Cita> buscarPorIdParaAdministrador(@Param("id") Long id);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.cliente
            JOIN FETCH c.empleado
            JOIN FETCH c.servicio
            WHERE c.id = :id
            """)
    java.util.Optional<Cita> buscarConfirmacionPorId(@Param("id") Long id);

    @Query("""
            SELECT c FROM Cita c
            JOIN FETCH c.cliente
            JOIN FETCH c.empleado
            JOIN FETCH c.servicio
            WHERE c.id = :id
              AND c.cliente.id = :clienteId
            """)
    Optional<Cita> buscarPorIdYClienteId(
            @Param("id") Long id,
            @Param("clienteId") Long clienteId);
}
