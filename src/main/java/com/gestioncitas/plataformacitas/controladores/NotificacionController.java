package com.gestioncitas.plataformacitas.controladores;

import com.gestioncitas.plataformacitas.modelos.Notificacion;
import com.gestioncitas.plataformacitas.servicios.NotificacionService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<Notificacion>> listar(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/{usuarioId}/conteo")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(Map.of("noLeidas", notificacionService.contarNoLeidas(usuarioId)));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }
}
