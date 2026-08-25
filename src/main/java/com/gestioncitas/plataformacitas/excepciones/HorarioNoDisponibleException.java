package com.gestioncitas.plataformacitas.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

<<<<<<< HEAD
/**
 * Excepción lanzada cuando el horario solicitado ya está ocupado por otra cita
 * (solapamiento) o el bloque no está disponible.
 *
 * Resulta en una respuesta HTTP 409 Conflict (SCRUM-1: anti-double booking).
 */
=======
>>>>>>> origin/develop
@ResponseStatus(HttpStatus.CONFLICT)
public class HorarioNoDisponibleException extends RuntimeException {

    public HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public HorarioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
