package com.gestioncitas.plataformacitas.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;

public interface HorarioDisponibilidadRepository extends JpaRepository<HorarioDisponibilidad, Long> {

	List<HorarioDisponibilidad> findByEmpleadoIdAndFechaAndEstado(Long empleadoId, LocalDate fecha, String estado);

	List<HorarioDisponibilidad> findByEmpleadoIdAndEstado(Long empleadoId, String estado);
}
