package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.dto.CitaClienteResponseDTO;
import com.gestioncitas.plataformacitas.dto.CitaResponseDTO;
import com.gestioncitas.plataformacitas.dto.HorarioDisponibleDTO;
import com.gestioncitas.plataformacitas.dto.ReservaCitaRequestDTO;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import com.gestioncitas.plataformacitas.servicios.CitaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

/**
 * API REST de citas (SCRUM-1, autor: Sam Alonso).
 * GET  /api/citas/disponibilidad -> bloques horarios libres
 * POST /api/citas/reservar       -> registra la reserva con anti-double booking
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping("/usuario")
    public ResponseEntity<List<CitaClienteResponseDTO>> listarCitasDelUsuario(
            @SessionAttribute(name = "usuario", required = false) Usuario usuario) {

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        if (!(usuario instanceof Cliente)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso exclusivo para clientes");
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(citaService.listarCitasDelCliente(usuario.getId()));
    }

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
            return ResponseEntity.ok(citaService.consultarDisponibilidadRango(servicioId, desde, hasta));
        }

        LocalDate fechaConsulta = (fecha != null) ? fecha : LocalDate.now();
        return ResponseEntity.ok(citaService.consultarDisponibilidad(servicioId, fechaConsulta));
    }

    @PostMapping("/reservar")
    public ResponseEntity<CitaResponseDTO> reservarCita(@Valid @RequestBody ReservaCitaRequestDTO request) {
        CitaResponseDTO respuesta = citaService.reservarCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
