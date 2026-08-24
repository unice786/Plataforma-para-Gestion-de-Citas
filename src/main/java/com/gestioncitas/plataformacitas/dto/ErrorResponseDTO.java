package com.gestioncitas.plataformacitas.dto;

import java.time.LocalDateTime;

/**
 * DTO de error estándar devuelto por {@code GlobalExceptionHandler} en todos
 * los casos de error de la API REST (SCRUM-1).
 */
public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;

    // ── Constructor vacío ──────────────────────────────────────────────────

    public ErrorResponseDTO() {}

    // ── Constructor completo ───────────────────────────────────────────────

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String mensaje) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
