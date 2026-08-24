package com.gestioncitas.plataformacitas.excepciones;

import com.gestioncitas.plataformacitas.dto.ErrorResponseDTO;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para la capa REST.
 *
 * <ul>
 *   <li>{@link HorarioNoDisponibleException}    → 409 Conflict</li>
 *   <li>{@link RecursoNoEncontradoException}     → 404 Not Found</li>
 *   <li>{@link MethodArgumentNotValidException}  → 400 Bad Request (Bean Validation)</li>
 *   <li>{@link Exception}                        → 500 Internal Server Error (fallback)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 409 Conflict: solapamiento de citas ───────────────────────────────

    @ExceptionHandler(HorarioNoDisponibleException.class)
    public ResponseEntity<ErrorResponseDTO> handleHorarioNoDisponible(
            HorarioNoDisponibleException ex) {

        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Horario no disponible",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ── 404 Not Found: entidad inexistente ────────────────────────────────

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex) {

        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ── 400 Bad Request: validaciones Bean Validation ─────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidacion(
            MethodArgumentNotValidException ex) {

        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Datos de entrada inválidos",
                errores
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 500 Internal Server Error: errores no controlados ────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenerico(Exception ex) {

        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ocurrió un error inesperado. Por favor contacte al administrador."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
