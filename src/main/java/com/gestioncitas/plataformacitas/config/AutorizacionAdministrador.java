package com.gestioncitas.plataformacitas.config;

import com.gestioncitas.plataformacitas.modelos.Administrador;
import com.gestioncitas.plataformacitas.modelos.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component("autorizacionAdministrador")
public class AutorizacionAdministrador implements HandlerInterceptor {

    private final String correoAdministrador;

    public AutorizacionAdministrador(
            @Value("${app.admin.correo:unice891@gmail.com}") String correoAdministrador) {
        this.correoAdministrador = correoAdministrador;
    }

    public boolean tieneAcceso(Usuario usuario) {
        return usuario != null
                && (usuario instanceof Administrador
                || (usuario.getCorreo() != null
                && correoAdministrador.equalsIgnoreCase(usuario.getCorreo())));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (!tieneAcceso(usuario)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso solo para administradores");
            return false;
        }

        return true;
    }
}
