package com.gestioncitas.plataformacitas.dto;

import java.time.LocalDateTime;

<<<<<<< HEAD
/**
 * DTO de error estándar devuelto por {@code GlobalExceptionHandler} en todos
 * los casos de error de la API REST (SCRUM-1).
 */
=======
>>>>>>> origin/develop
public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;

<<<<<<< HEAD
    // ── Constructor vacío ──────────────────────────────────────────────────

    public ErrorResponseDTO() {}

    // ── Constructor completo ───────────────────────────────────────────────

=======
    public ErrorResponseDTO() {}

>>>>>>> origin/develop
    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String mensaje) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
    }

<<<<<<< HEAD
    // ── Getters y Setters ──────────────────────────────────────────────────

=======
>>>>>>> origin/develop
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
