package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServicioRepository extends JpaRepository<Servicio, Long>, JpaSpecificationExecutor<Servicio> {

    List<Servicio> findAllByOrderByNombreAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Servicio> findByActivoTrueOrderByNombreAsc();

    @Query("""
            SELECT DISTINCT s FROM Servicio s
            LEFT JOIN FETCH s.categoria
            WHERE s.activo = true
            ORDER BY s.nombre ASC
            """)
    List<Servicio> findActivosConCategoriaOrderByNombreAsc();

    @Query("""
            SELECT DISTINCT s FROM Servicio s
            LEFT JOIN FETCH s.categoria
            ORDER BY s.nombre ASC
            """)
    List<Servicio> findAllConCategoriaOrderByNombreAsc();
}
