package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.ServicioResponseDTO;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para consultar el catálogo de servicios disponibles.
 * Expone GET /api/servicios para la reserva en el frontend.
 */
@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class ServicioRestController {

    private final ServicioRepository servicioRepository;

    public ServicioRestController(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    /**
     * Devuelve la lista de servicios activos ordenados alfabéticamente.
     */
    @GetMapping
    public ResponseEntity<List<ServicioResponseDTO>> listarServiciosActivos() {
        List<Servicio> servicios = servicioRepository.findByActivoTrueOrderByNombreAsc();
        List<ServicioResponseDTO> dtos = servicios.stream()
                .map(s -> new ServicioResponseDTO(
                        s.getId(),
                        s.getNombre(),
                        s.getDescripcion(),
                        s.getDuracionMinutos(),
                        s.getPrecio(),
                        s.getCategoria() != null ? s.getCategoria().getNombre() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
