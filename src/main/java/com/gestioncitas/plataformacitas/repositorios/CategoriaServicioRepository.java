package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.CategoriaServicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, Long> {

    List<CategoriaServicio> findAllByOrderByNombreAsc();
}
