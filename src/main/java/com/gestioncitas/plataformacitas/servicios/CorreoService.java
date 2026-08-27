package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.modelos.Usuario;

/**
 * Servicio de negocio para el envío de correos electrónicos (SCRUM-7).
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Enviar correos de verificación de cuenta.</li>
 *   <li>Enviar correos de recuperación de contraseña.</li>
 * </ul>
 */
public interface CorreoService {

    /**
     * Envía un correo de verificación de cuenta al usuario.
     *
     * @param usuario Usuario al que enviar el correo de verificación
     */
    void enviarVerificacion(Usuario usuario);

    /**
     * Envía un correo de solicitud de restablecimiento de contraseña.
     *
     * @param usuario Usuario al que enviar el correo de recuperación
     */
    void enviarRecuperacion(Usuario usuario);
}