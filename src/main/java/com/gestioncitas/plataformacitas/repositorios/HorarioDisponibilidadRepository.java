package com.gestioncitas.plataformacitas.repositorios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;

public interface HorarioDisponibilidadRepository extends JpaRepository<HorarioDisponibilidad, Long> {

	List<HorarioDisponibilidad> findByEmpleadoIdAndFechaAndEstado(Long empleadoId, LocalDate fecha, String estado);

	List<HorarioDisponibilidad> findByEmpleadoIdAndEstado(Long empleadoId, String estado);

	List<HorarioDisponibilidad> findByEstadoOrderByFechaAscHoraInicioAsc(String estado);

	List<HorarioDisponibilidad> findByEmpleadoIdOrderByFechaAscHoraInicioAsc(Long empleadoId);

	List<HorarioDisponibilidad> findByFechaBetweenOrderByFechaAscHoraInicioAsc(LocalDate desde, LocalDate hasta);

	List<HorarioDisponibilidad> findByFechaGreaterThanEqualOrderByFechaAscHoraInicioAsc(LocalDate fecha);

	List<HorarioDisponibilidad> findByFechaLessThanEqualOrderByFechaAscHoraInicioAsc(LocalDate fecha);

	boolean existsByEmpleadoIdAndFechaAndHoraInicioAndHoraFin(
			Long empleadoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);

	boolean existsByEmpleadoIdAndFechaAndHoraInicioAndHoraFinAndIdNot(
			Long empleadoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Long id);

	@Query("""
			SELECT h FROM HorarioDisponibilidad h
			JOIN FETCH h.empleado
			ORDER BY h.fecha ASC, h.horaInicio ASC
			""")
	List<HorarioDisponibilidad> findAllConEmpleadoOrderByFechaAsc();

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
			@Param("estado") String estado
	);

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
			@Param("estado") String estado
	);
}
