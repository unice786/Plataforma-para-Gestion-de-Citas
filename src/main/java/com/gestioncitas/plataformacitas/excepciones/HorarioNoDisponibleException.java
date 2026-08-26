package com.gestioncitas.plataformacitas.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HorarioNoDisponibleException extends RuntimeException {

    public HorarioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public HorarioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
