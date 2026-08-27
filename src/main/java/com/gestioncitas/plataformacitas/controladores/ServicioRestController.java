package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.ServicioResponseDTO;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST del catálogo para el formulario de reserva (SCRUM-1).
 * Expone GET /api/servicios con los servicios activos y filtros opcionales.
 */
@RestController
@RequestMapping("/api/servicios")
public class ServicioRestController {

    private final ServicioRepository servicioRepository;

    public ServicioRestController(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ServicioResponseDTO>> listarServiciosActivos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Integer duracion,
            @RequestParam(required = false) String query) {
        Specification<Servicio> filtros = Specification.where(activo())
                .and(categoriaEs(categoria))
                .and(precioMayorOIgual(precioMin))
                .and(precioMenorOIgual(precioMax))
                .and(duracionEs(duracion))
                .and(coincideTexto(query));

        List<Servicio> servicios = servicioRepository.findAll(filtros, Sort.by(Sort.Direction.ASC, "nombre"));
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

    private Specification<Servicio> activo() {
        return (root, query, builder) -> builder.isTrue(root.get("activo"));
    }

    private Specification<Servicio> categoriaEs(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return null;
        }
        String valor = categoria.trim().toLowerCase();
        return (root, query, builder) -> builder.equal(
                builder.lower(root.join("categoria").get("nombre")), valor);
    }

    private Specification<Servicio> precioMayorOIgual(BigDecimal precioMin) {
        return precioMin == null ? null : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("precio"), precioMin);
    }

    private Specification<Servicio> precioMenorOIgual(BigDecimal precioMax) {
        return precioMax == null ? null : (root, query, builder) -> builder.lessThanOrEqualTo(root.get("precio"), precioMax);
    }

    private Specification<Servicio> duracionEs(Integer duracion) {
        return duracion == null ? null : (root, query, builder) -> builder.equal(root.get("duracionMinutos"), duracion);
    }

    private Specification<Servicio> coincideTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String patron = "%" + texto.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("nombre")), patron),
                builder.like(builder.lower(builder.coalesce(root.get("descripcion"), "")), patron));
    }
}
