package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.modelos.Usuario;
import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.correo.habilitado:false}")
    private boolean correoHabilitado;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${app.correo.remitente:no-reply@plataformacitas.com}")
    private String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void enviarVerificacion(Usuario usuario) {
        String enlace = appUrl + "/verificar?token=" + usuario.getTokenVerificacion();
        String cuerpo = """
                <html><body style="font-family: Arial, sans-serif;">
                  <h2>¡Hola, %s!</h2>
                  <p>Gracias por registrarte en Plataforma de Citas. Confirma tu correo haciendo clic en el siguiente enlace:</p>
                  <p><a href="%s" style="background:#0d6efd;color:#fff;padding:10px 20px;text-decoration:none;border-radius:5px;">Verificar mi cuenta</a></p>
                  <p>El enlace es válido por 24 horas.</p>
                  <p>Si no creaste esta cuenta, ignora este correo.</p>
                </body></html>
                """.formatted(usuario.getNombre(), enlace);

        enviar(usuario, "Confirma tu cuenta en Plataforma de Citas", cuerpo, enlace);
    }

    public void enviarRecuperacion(Usuario usuario) {
        String enlace = appUrl + "/recuperar?token=" + codificarToken(usuario.getTokenRecuperacion());
        String cuerpo = """
                <html><body style="font-family: Arial, sans-serif;">
                  <h2>Hola, %s</h2>
                  <p>Recibimos una solicitud para restablecer tu contraseña. Haz clic en el siguiente enlace para crear una nueva:</p>
                  <p><a href="%s" style="background:#0d6efd;color:#fff;padding:10px 20px;text-decoration:none;border-radius:5px;">Restablecer contraseña</a></p>
                  <p>El enlace es válido por 30 minutos.</p>
                  <p>Si no solicitaste este cambio, ignora este correo.</p>
                </body></html>
                """.formatted(usuario.getNombre(), enlace);

        enviar(usuario, "Restablece tu contraseña en Plataforma de Citas", cuerpo, enlace);
    }

    private String codificarToken(String token) {
        try {
            return URLEncoder.encode(token, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return token;
        }
    }

    private void enviar(Usuario usuario, String asunto, String cuerpoHtml, String enlace) {
        if (!correoHabilitado || mailSenderProvider.getIfAvailable() == null) {
            log.warn("[DEV] Envío de correo deshabilitado. Enlace para {}: {}", usuario.getCorreo(), enlace);
            return;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(usuario.getCorreo());
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            mailSender.send(mensaje);
            log.info("Correo enviado a {}", usuario.getCorreo());
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", usuario.getCorreo(), e.getMessage());
        }
    }
}