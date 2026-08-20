package com.gestioncitas.plataformacitas.repositorios;

import com.gestioncitas.plataformacitas.modelos.Cita;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    @EntityGraph(attributePaths = "servicio")
    List<Cita> findByClienteIdOrderByFechaDescHoraDesc(Long clienteId);
}
