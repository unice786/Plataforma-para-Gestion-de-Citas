package com.gestioncitas.plataformacitas.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestioncitas.plataformacitas.modelos.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
}
