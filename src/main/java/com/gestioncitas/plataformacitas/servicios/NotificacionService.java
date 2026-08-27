package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.modelos.Notificacion;
import java.util.List;

public interface NotificacionService {

    Notificacion crear(Long usuarioId, String mensaje, String tipo);

    List<Notificacion> listarPorUsuario(Long usuarioId);

    long contarNoLeidas(Long usuarioId);

    void marcarComoLeida(Long notificacionId);

    void notificarAdmins(String mensaje, String tipo);
}
