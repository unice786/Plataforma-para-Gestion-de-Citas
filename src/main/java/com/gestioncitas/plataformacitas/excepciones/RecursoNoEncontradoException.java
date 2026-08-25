package com.gestioncitas.plataformacitas.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

<<<<<<< HEAD
/**
 * Excepción lanzada cuando una entidad requerida (Cliente, Empleado, Servicio)
 * no se encuentra en la base de datos.
 *
 * Resulta en una respuesta HTTP 404 Not Found (SCRUM-1).
 */
=======
>>>>>>> origin/develop
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(String.format("%s con id=%d no fue encontrado", recurso, id));
    }
}
