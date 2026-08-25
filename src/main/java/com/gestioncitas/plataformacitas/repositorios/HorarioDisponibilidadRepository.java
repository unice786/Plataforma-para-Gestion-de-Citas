package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.EstadoHorario;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link HorarioDisponibilidad}.
 *
 * Provee métodos para consultar los bloques de disponibilidad de empleados
 * que ofrecen un servicio específico (SCRUM-1: endpoint GET disponibilidad).
 */
@Repository
public interface HorarioDisponibilidadRepository extends JpaRepository<HorarioDisponibilidad, Long> {

    /**
     * Devuelve todos los bloques de horario DISPONIBLE de los empleados
     * que ofrecen el servicio indicado en una fecha exacta.
     *
     * @param servicioId ID del servicio solicitado
     * @param fecha      Fecha a consultar
     * @param estado     Estado del horario (normalmente DISPONIBLE)
     * @return Lista de bloques disponibles ordenados por hora de inicio
     */
    @Query("""
            SELECT h FROM HorarioDisponibilidad h
            JOIN h.empleado e
            JOIN e.servicios s
            WHERE s.id = :servicioId
              AND h.fecha = :fecha
              AND h.estado = :estado
            ORDER BY e.id ASC, h.horaInicio ASC
            """)
    List<HorarioDisponibilidad> findDisponiblesByServicioAndFecha(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoHorario estado
    );

    /**
     * Devuelve todos los bloques de horario DISPONIBLE de los empleados
     * que ofrecen el servicio indicado dentro de un rango de fechas.
     *
     * @param servicioId ID del servicio solicitado
     * @param desde      Fecha de inicio del rango (inclusive)
     * @param hasta      Fecha de fin del rango (inclusive)
     * @param estado     Estado del horario (normalmente DISPONIBLE)
     * @return Lista de bloques disponibles ordenados por fecha y hora
     */
    @Query("""
            SELECT h FROM HorarioDisponibilidad h
            JOIN h.empleado e
            JOIN e.servicios s
            WHERE s.id = :servicioId
              AND h.fecha BETWEEN :desde AND :hasta
              AND h.estado = :estado
            ORDER BY h.fecha ASC, e.id ASC, h.horaInicio ASC
            """)
    List<HorarioDisponibilidad> findDisponiblesByServicioAndRangoFechas(
            @Param("servicioId") Long servicioId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("estado") EstadoHorario estado
    );
}
