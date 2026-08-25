package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Cita}.
 *
 * La detección de solapamiento (anti-double booking) se realiza en Java
 * dentro de {@code CitaService}, cargando las citas activas del empleado en la
 * fecha solicitada y verificando el intervalo. Este enfoque es 100% portable
 * entre H2 (desarrollo) y MySQL (producción) sin depender de funciones nativas.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Devuelve todas las citas de un cliente ordenadas por fecha y hora descendente.
     *
     * @param clienteId ID del cliente
     * @return Lista de citas del cliente
     */
    @EntityGraph(attributePaths = "servicio")
    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);

    /**
     * Devuelve todas las citas activas (no canceladas) de un empleado en una
     * fecha específica, ordenadas cronológicamente.
     *
     * <p>Utilizado por {@code CitaService} para:
     * <ol>
     *   <li>Verificar solapamiento antes de crear una nueva cita.</li>
     *   <li>Calcular los slots libres al consultar disponibilidad.</li>
     * </ol>
     *
     * @param empleadoId ID del empleado
     * @param fecha      Fecha a consultar
     * @param estados    Lista de estados activos a incluir (ej. PENDIENTE, CONFIRMADA)
     * @return Lista de citas activas ordenadas por hora ascendente
     */
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
}