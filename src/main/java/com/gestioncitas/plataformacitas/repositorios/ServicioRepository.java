package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    List<Servicio> findAllByOrderByNombreAsc();

    List<Servicio> findByActivoTrueOrderByNombreAsc();
}
