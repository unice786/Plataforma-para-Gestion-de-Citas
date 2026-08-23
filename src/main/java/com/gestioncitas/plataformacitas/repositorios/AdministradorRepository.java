package com.gestioncitas.plataformacitas.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gestioncitas.plataformacitas.modelos.Administrador;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
}