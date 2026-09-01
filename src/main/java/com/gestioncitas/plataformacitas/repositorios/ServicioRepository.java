package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Servicio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

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

    @Query("""
            SELECT s FROM Servicio s
            JOIN FETCH s.categoria
            WHERE s.id = :id
            """)
    Optional<Servicio> findConCategoriaPorId(@Param("id") Long id);

    long countByCategoriaId(Long categoriaId);
}
