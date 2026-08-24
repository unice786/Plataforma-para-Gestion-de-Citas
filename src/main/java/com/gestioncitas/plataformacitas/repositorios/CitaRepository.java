package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    @EntityGraph(attributePaths = "servicio")
    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);

    @Query("""
            SELECT c FROM Cita c JOIN FETCH c.servicio
            WHERE c.empleado.id = :empleadoId AND c.fecha = :fecha AND c.estado IN :estados
            ORDER BY c.hora ASC
            """)
    List<Cita> findCitasActivasByEmpleadoAndFecha(
            @Param("empleadoId") Long empleadoId,
            @Param("fecha") LocalDate fecha,
            @Param("estados") List<EstadoCita> estados);
}
