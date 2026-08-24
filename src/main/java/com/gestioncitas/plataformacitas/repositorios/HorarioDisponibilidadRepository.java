package com.gestioncitas.plataformacitas.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;

public interface HorarioDisponibilidadRepository extends JpaRepository<HorarioDisponibilidad, Long> {

	List<HorarioDisponibilidad> findByEmpleadoIdAndFechaAndEstado(Long empleadoId, LocalDate fecha, String estado);

	List<HorarioDisponibilidad> findByEmpleadoIdAndEstado(Long empleadoId, String estado);

	@Query("""
			SELECT h FROM HorarioDisponibilidad h JOIN h.empleado e JOIN e.servicios s
			WHERE s.id = :servicioId AND h.fecha = :fecha AND h.estado = :estado
			ORDER BY e.id ASC, h.horaInicio ASC
			""")
	List<HorarioDisponibilidad> findDisponiblesByServicioAndFecha(
			@Param("servicioId") Long servicioId, @Param("fecha") LocalDate fecha,
			@Param("estado") String estado);

	@Query("""
			SELECT h FROM HorarioDisponibilidad h JOIN h.empleado e JOIN e.servicios s
			WHERE s.id = :servicioId AND h.fecha BETWEEN :desde AND :hasta AND h.estado = :estado
			ORDER BY h.fecha ASC, e.id ASC, h.horaInicio ASC
			""")
	List<HorarioDisponibilidad> findDisponiblesByServicioAndRangoFechas(
			@Param("servicioId") Long servicioId, @Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta, @Param("estado") String estado);
}
