package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.CitaResponseDTO;
import com.gestioncitas.plataformacitas.dto.HorarioDisponibleDTO;
import com.gestioncitas.plataformacitas.dto.ReservaCitaRequestDTO;
import com.gestioncitas.plataformacitas.servicios.CitaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de citas y disponibilidad (SCRUM-1).
 *
 * <p>Endpoints expuestos:
 * <ul>
 *   <li>{@code GET /api/citas/disponibilidad} - Consulta de bloques horarios libres</li>
 *   <li>{@code POST /api/citas/reservar} - Procesar y registrar solicitud de reserva</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * Endpoint GET para consultar los bloques de fecha y hora libres según el servicio seleccionado.
     * Soporta consulta por fecha puntual o por rango de fechas (desde/hasta).
     *
     * @param servicioId ID del servicio a consultar (obligatorio)
     * @param fecha      Fecha puntual (opcional si se usa rango)
     * @param desde      Fecha inicial del rango (opcional)
     * @param hasta      Fecha final del rango (opcional)
     * @return Lista de bloques horarios disponibles
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<HorarioDisponibleDTO>> consultarDisponibilidad(
            @RequestParam("servicioId") Long servicioId,
            @RequestParam(value = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        if (desde != null && hasta != null) {
            List<HorarioDisponibleDTO> slots = citaService.consultarDisponibilidadRango(servicioId, desde, hasta);
            return ResponseEntity.ok(slots);
        }

        LocalDate fechaConsulta = (fecha != null) ? fecha : LocalDate.now();
        List<HorarioDisponibleDTO> slots = citaService.consultarDisponibilidad(servicioId, fechaConsulta);
        return ResponseEntity.ok(slots);
    }

    /**
     * Endpoint POST para procesar y registrar la solicitud de reserva.
     * Aplica validaciones Bean Validation y reglas anti-double booking en el servicio.
     *
     * @param request DTO con clienteId, empleadoId, servicioId, fecha y hora
     * @return 201 Created con CitaResponseDTO y mensaje de confirmación
     */
    @PostMapping("/reservar")
    public ResponseEntity<CitaResponseDTO> reservarCita(
            @Valid @RequestBody ReservaCitaRequestDTO request) {

        CitaResponseDTO respuesta = citaService.reservarCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}