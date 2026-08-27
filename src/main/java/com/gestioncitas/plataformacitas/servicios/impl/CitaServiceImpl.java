package com.gestioncitas.plataformacitas.servicios.impl;

import com.gestioncitas.plataformacitas.dto.CitaResponseDTO;
import com.gestioncitas.plataformacitas.dto.EdicionCitaRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioDisponibleDTO;
import com.gestioncitas.plataformacitas.dto.ReprogramarCitaDTO;
import com.gestioncitas.plataformacitas.dto.ReservaCitaRequestDTO;
import com.gestioncitas.plataformacitas.excepciones.HorarioNoDisponibleException;
import com.gestioncitas.plataformacitas.excepciones.RecursoNoEncontradoException;
import com.gestioncitas.plataformacitas.modelos.Cita;
import com.gestioncitas.plataformacitas.modelos.Cliente;
import com.gestioncitas.plataformacitas.modelos.Empleado;
import com.gestioncitas.plataformacitas.modelos.EstadoCita;
import com.gestioncitas.plataformacitas.modelos.EstadoHorario;
import com.gestioncitas.plataformacitas.modelos.HorarioDisponibilidad;
import com.gestioncitas.plataformacitas.modelos.Servicio;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.repositorios.ClienteRepository;
import com.gestioncitas.plataformacitas.repositorios.EmpleadoRepository;
import com.gestioncitas.plataformacitas.repositorios.HorarioDisponibilidadRepository;
import com.gestioncitas.plataformacitas.repositorios.ServicioRepository;
import com.gestioncitas.plataformacitas.servicios.CitaService;
import com.gestioncitas.plataformacitas.servicios.NotificacionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CitaServiceImpl implements CitaService {

    private static final List<EstadoCita> ESTADOS_ACTIVOS = List.of(
            EstadoCita.PENDIENTE,
            EstadoCita.CONFIRMADA
    );

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioRepository servicioRepository;
    private final HorarioDisponibilidadRepository horarioRepository;
    private final NotificacionService notificacionService;

    public CitaServiceImpl(CitaRepository citaRepository,
                           ClienteRepository clienteRepository,
                           EmpleadoRepository empleadoRepository,
                           ServicioRepository servicioRepository,
                           HorarioDisponibilidadRepository horarioRepository,
                           NotificacionService notificacionService) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.horarioRepository = horarioRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    public List<HorarioDisponibleDTO> consultarDisponibilidad(Long servicioId, LocalDate fecha) {
        Servicio servicio = buscarServicio(servicioId);
        int duracion = servicio.getDuracionMinutos();

        List<HorarioDisponibilidad> bloques = horarioRepository
                .findDisponiblesByServicioAndFecha(servicioId, fecha, EstadoHorario.DISPONIBLE.name());

        List<HorarioDisponibleDTO> slotsLibres = new ArrayList<>();

        for (HorarioDisponibilidad bloque : bloques) {
            Empleado empleado = bloque.getEmpleado();
            List<Cita> citasExistentes = citaRepository
                    .findCitasActivasByEmpleadoAndFecha(empleado.getId(), fecha, ESTADOS_ACTIVOS);

            LocalTime cursor = bloque.getHoraInicio();
            LocalTime finBloque = bloque.getHoraFin();

            while (!cursor.plusMinutes(duracion).isAfter(finBloque)) {
                LocalTime slotFin = cursor.plusMinutes(duracion);

                if (!haySolapamiento(cursor, slotFin, citasExistentes, duracion)) {
                    slotsLibres.add(new HorarioDisponibleDTO(
                            empleado.getId(),
                            empleado.getNombre(),
                            fecha,
                            cursor,
                            slotFin
                    ));
                }
                cursor = slotFin;
            }
        }

        return slotsLibres;
    }

    @Override
    public List<HorarioDisponibleDTO> consultarDisponibilidadRango(
            Long servicioId, LocalDate desde, LocalDate hasta) {

        Servicio servicio = buscarServicio(servicioId);
        int duracion = servicio.getDuracionMinutos();

        List<HorarioDisponibilidad> bloques = horarioRepository
                .findDisponiblesByServicioAndRangoFechas(
                        servicioId, desde, hasta, EstadoHorario.DISPONIBLE.name());

        List<HorarioDisponibleDTO> slotsLibres = new ArrayList<>();

        for (HorarioDisponibilidad bloque : bloques) {
            Empleado empleado = bloque.getEmpleado();
            List<Cita> citasExistentes = citaRepository
                    .findCitasActivasByEmpleadoAndFecha(
                            empleado.getId(), bloque.getFecha(), ESTADOS_ACTIVOS);

            LocalTime cursor = bloque.getHoraInicio();
            LocalTime finBloque = bloque.getHoraFin();

            while (!cursor.plusMinutes(duracion).isAfter(finBloque)) {
                LocalTime slotFin = cursor.plusMinutes(duracion);

                if (!haySolapamiento(cursor, slotFin, citasExistentes, duracion)) {
                    slotsLibres.add(new HorarioDisponibleDTO(
                            empleado.getId(),
                            empleado.getNombre(),
                            bloque.getFecha(),
                            cursor,
                            slotFin
                    ));
                }
                cursor = slotFin;
            }
        }

        return slotsLibres;
    }

    @Override
    @Transactional
    public CitaResponseDTO reservarCita(ReservaCitaRequestDTO request) {
        Cliente cliente = buscarCliente(request.getClienteId());
        Empleado empleado = buscarEmpleado(request.getEmpleadoId());
        Servicio servicio = buscarServicio(request.getServicioId());

        LocalTime horaInicio = request.getHora();
        LocalTime horaFin = horaInicio.plusMinutes(servicio.getDuracionMinutos());

        List<Cita> citasExistentes = citaRepository.findCitasActivasByEmpleadoAndFecha(
                empleado.getId(), request.getFecha(), ESTADOS_ACTIVOS);

        if (haySolapamiento(horaInicio, horaFin, citasExistentes, servicio.getDuracionMinutos())) {
            throw new HorarioNoDisponibleException(
                    String.format(
                            "El empleado '%s' ya tiene una cita activa que se solapa con el horario %s-%s del %s.",
                            empleado.getNombre(),
                            horaInicio,
                            horaFin,
                            request.getFecha()
                    )
            );
        }

        Cita nuevaCita = new Cita();
        nuevaCita.setCliente(cliente);
        nuevaCita.setEmpleado(empleado);
        nuevaCita.setServicio(servicio);
        nuevaCita.setFecha(request.getFecha());
        nuevaCita.setHora(horaInicio);
        nuevaCita.setEstado(EstadoCita.PENDIENTE);
        nuevaCita.setFechaRegistro(LocalDateTime.now());

        Cita citaGuardada = citaRepository.save(nuevaCita);

        String msgCliente = String.format(
                "¡Cita reservada! Tu cita para '%s' con %s está agendada para el %s a las %s.",
                servicio.getNombre(), empleado.getNombre(), citaGuardada.getFecha(), citaGuardada.getHora());
        notificacionService.crear(cliente.getId(), msgCliente, "RESERVA");

        String msgAdmin = String.format(
                "El cliente %s reservó una cita para '%s' con %s el %s a las %s.",
                cliente.getNombre(), servicio.getNombre(), empleado.getNombre(),
                citaGuardada.getFecha(), citaGuardada.getHora());
        notificacionService.crear(empleado.getId(), msgAdmin, "RESERVA");
        notificacionService.notificarAdmins(msgAdmin, "RESERVA");

        return new CitaResponseDTO(
                citaGuardada.getId(),
                cliente.getNombre(),
                empleado.getNombre(),
                servicio.getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora(),
                citaGuardada.getEstado(),
                citaGuardada.getFechaRegistro(),
                msgCliente
        );
    }

    @Override
    @Transactional
    public void editarCita(Long citaId, EdicionCitaRequestDTO request) {
        Cita cita = citaRepository.buscarPorIdParaAdministrador(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("No se puede modificar una cita cancelada.");
        }

        Servicio nuevoServicio = buscarServicio(request.getServicioId());
        if (!Boolean.TRUE.equals(nuevoServicio.getActivo())) {
            throw new IllegalArgumentException("No se puede asignar un servicio inactivo.");
        }

        LocalTime nuevoFin = request.getHora().plusMinutes(nuevoServicio.getDuracionMinutos());
        List<Cita> citasExistentes = citaRepository.findCitasActivasByEmpleadoAndFecha(
                cita.getEmpleado().getId(), request.getFecha(), ESTADOS_ACTIVOS);
        boolean hayConflicto = citasExistentes.stream()
                .filter(otraCita -> !otraCita.getId().equals(citaId))
                .anyMatch(otraCita -> seSolapa(request.getHora(), nuevoFin, otraCita,
                        nuevoServicio.getDuracionMinutos()));
        if (hayConflicto) {
            throw new HorarioNoDisponibleException("El horario seleccionado se solapa con otra cita activa del empleado.");
        }

        String detalle = String.format("Cita modificada: %s %s, servicio %s.",
                request.getFecha(), request.getHora(), nuevoServicio.getNombre());
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setServicio(nuevoServicio);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio(detalle);
        citaRepository.save(cita);

        String msgCliente = String.format(
                "Tu cita fue modificada. Nuevo servicio: '%s', fecha: %s, hora: %s.",
                nuevoServicio.getNombre(), request.getFecha(), request.getHora());
        notificacionService.crear(cita.getCliente().getId(), msgCliente, "EDICION");

        String msgEmpleado = String.format(
                "La cita de %s fue modificada por el administrador. Servicio: '%s', fecha: %s, hora: %s.",
                cita.getCliente().getNombre(), nuevoServicio.getNombre(), request.getFecha(), request.getHora());
        notificacionService.crear(cita.getEmpleado().getId(), msgEmpleado, "EDICION");
    }

    @Override
    @Transactional
    public void cancelarCita(Long citaId) {
        Cita cita = citaRepository.buscarPorIdParaAdministrador(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("La cita ya se encontraba cancelada.");
        }
        cita.setEstado(EstadoCita.CANCELADA);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio("Cita cancelada por el administrador.");
        citaRepository.save(cita);

        String msgCliente = String.format(
                "Tu cita para '%s' con %s el %s a las %s fue cancelada por el administrador.",
                cita.getServicio().getNombre(), cita.getEmpleado().getNombre(),
                cita.getFecha(), cita.getHora());
        notificacionService.crear(cita.getCliente().getId(), msgCliente, "CANCELACION");

        String msgEmpleado = String.format(
                "La cita de %s para '%s' el %s a las %s fue cancelada por el administrador.",
                cita.getCliente().getNombre(), cita.getServicio().getNombre(),
                cita.getFecha(), cita.getHora());
        notificacionService.crear(cita.getEmpleado().getId(), msgEmpleado, "CANCELACION");
    }

    @Override
    @Transactional
    public CitaResponseDTO cancelarCitaCliente(Long citaId, Long clienteId) {
        Cita cita = citaRepository.findByIdAndClienteId(citaId, clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cita con id=" + citaId + " no encontrada para el cliente"));

        if (cita.getEstado() != EstadoCita.PENDIENTE && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new HorarioNoDisponibleException(
                    String.format("La cita con id=%d tiene estado '%s' y no puede ser cancelada.",
                            cita.getId(), cita.getEstado()));
        }

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio("Cita cancelada por el cliente.");
        Cita citaGuardada = citaRepository.save(cita);

        String msgCliente = String.format("Tu cita para '%s' con %s el %s a las %s ha sido cancelada.",
                cita.getServicio().getNombre(),
                cita.getEmpleado().getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora());
        notificacionService.crear(clienteId, msgCliente, "CANCELACION");

        String msgAdmin = String.format("El cliente %s canceló la cita para '%s' con %s el %s a las %s.",
                cita.getCliente().getNombre(),
                cita.getServicio().getNombre(),
                cita.getEmpleado().getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora());
        notificacionService.crear(cita.getEmpleado().getId(), msgAdmin, "CANCELACION");
        notificacionService.notificarAdmins(msgAdmin, "CANCELACION");

        return new CitaResponseDTO(
                citaGuardada.getId(),
                cita.getCliente().getNombre(),
                cita.getEmpleado().getNombre(),
                cita.getServicio().getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora(),
                citaGuardada.getEstado(),
                citaGuardada.getFechaRegistro(),
                msgCliente
        );
    }

    @Override
    @Transactional
    public CitaResponseDTO reprogramarCita(ReprogramarCitaDTO request) {
        Cita cita = citaRepository.findByIdAndClienteId(request.getCitaId(), request.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cita con id=" + request.getCitaId() + " no encontrada para el cliente"));

        if (cita.getEstado() != EstadoCita.PENDIENTE && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new HorarioNoDisponibleException(
                    String.format(
                            "La cita con id=%d tiene estado '%s' y no puede ser reprogramada.",
                            cita.getId(), cita.getEstado()
                    )
            );
        }

        Servicio servicio = cita.getServicio();
        Empleado empleado = cita.getEmpleado();
        LocalDate fechaAnterior = cita.getFecha();
        LocalTime horaAnterior = cita.getHora();
        LocalTime nuevaHoraInicio = request.getHora();
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(servicio.getDuracionMinutos());

        LocalDateTime nuevoInicio = LocalDateTime.of(request.getFecha(), nuevaHoraInicio);
        if (nuevoInicio.isBefore(LocalDateTime.now())) {
            throw new HorarioNoDisponibleException("La nueva fecha y hora no pueden estar en el pasado.");
        }

        List<Cita> citasExistentes = citaRepository
                .findCitasActivasByEmpleadoAndFechaExcludingId(
                        empleado.getId(), request.getFecha(), ESTADOS_ACTIVOS, cita.getId());

        if (haySolapamiento(nuevaHoraInicio, nuevaHoraFin, citasExistentes, servicio.getDuracionMinutos())) {
            throw new HorarioNoDisponibleException(
                    String.format(
                            "El empleado '%s' ya tiene una cita activa que se solapa con el horario %s-%s del %s.",
                            empleado.getNombre(),
                            nuevaHoraInicio,
                            nuevaHoraFin,
                            request.getFecha()
                    )
            );
        }

        cita.setFecha(request.getFecha());
        cita.setHora(nuevaHoraInicio);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio(String.format(
                "Cita reprogramada: de %s %s a %s %s.",
                fechaAnterior, horaAnterior, request.getFecha(), nuevaHoraInicio));
        Cita citaGuardada = citaRepository.save(cita);

        String msgCliente = String.format(
                "¡Cita reprogramada! Tu cita para '%s' con %s ha sido movida al %s a las %s.",
                servicio.getNombre(), empleado.getNombre(),
                citaGuardada.getFecha(), citaGuardada.getHora());
        notificacionService.crear(cita.getCliente().getId(), msgCliente, "REPROGRAMACION");

        String msgAdmin = String.format(
                "El cliente %s reprogramó la cita para '%s' con %s del %s %s al %s a las %s.",
                cita.getCliente().getNombre(), servicio.getNombre(), empleado.getNombre(),
                fechaAnterior, horaAnterior, citaGuardada.getFecha(), citaGuardada.getHora());
        notificacionService.crear(empleado.getId(), msgAdmin, "REPROGRAMACION");
        notificacionService.notificarAdmins(msgAdmin, "REPROGRAMACION");

        return new CitaResponseDTO(
                citaGuardada.getId(),
                cita.getCliente().getNombre(),
                empleado.getNombre(),
                servicio.getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora(),
                citaGuardada.getEstado(),
                citaGuardada.getFechaRegistro(),
                msgCliente
        );
    }

    @Override
    @Transactional
    public void eliminarCitaCliente(Long citaId, Long clienteId) {
        Cita cita = citaRepository.findByIdAndClienteId(citaId, clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cita con id=" + citaId + " no encontrada para el cliente"));

        if (cita.getEstado() != EstadoCita.CANCELADA) {
            throw new IllegalStateException("Solo se pueden eliminar citas canceladas del historial.");
        }

        citaRepository.delete(cita);
    }

    @Override
    @Transactional
    public void confirmarCita(Long citaId, Long empleadoId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));

        if (!cita.getEmpleado().getId().equals(empleadoId)) {
            throw new RecursoNoEncontradoException("Cita", citaId);
        }
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new IllegalStateException(
                    String.format("La cita con id=%d tiene estado '%s' y no puede ser confirmada.", citaId, cita.getEstado()));
        }

        cita.setEstado(EstadoCita.CONFIRMADA);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio("Cita confirmada por el empleado.");
        citaRepository.save(cita);

        String msgCliente = String.format(
                "Tu cita para '%s' con %s el %s a las %s ha sido confirmada.",
                cita.getServicio().getNombre(), cita.getEmpleado().getNombre(),
                cita.getFecha(), cita.getHora());
        notificacionService.crear(cita.getCliente().getId(), msgCliente, "CONFIRMACION");

        String msgAdmin = String.format(
                "El empleado %s confirmó la cita de %s para '%s' el %s a las %s.",
                cita.getEmpleado().getNombre(), cita.getCliente().getNombre(),
                cita.getServicio().getNombre(), cita.getFecha(), cita.getHora());
        notificacionService.notificarAdmins(msgAdmin, "CONFIRMACION");
    }

    @Override
    @Transactional
    public void completarCita(Long citaId, Long empleadoId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));

        if (!cita.getEmpleado().getId().equals(empleadoId)) {
            throw new RecursoNoEncontradoException("Cita", citaId);
        }
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new IllegalStateException(
                    String.format("La cita con id=%d tiene estado '%s' y no puede ser completada.", citaId, cita.getEstado()));
        }

        cita.setEstado(EstadoCita.COMPLETADA);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio("Cita completada por el empleado.");
        citaRepository.save(cita);

        String msgCliente = String.format(
                "Tu cita para '%s' con %s el %s a las %s ha sido completada.",
                cita.getServicio().getNombre(), cita.getEmpleado().getNombre(),
                cita.getFecha(), cita.getHora());
        notificacionService.crear(cita.getCliente().getId(), msgCliente, "COMPLETADA");

        String msgAdmin = String.format(
                "El empleado %s completó la cita de %s para '%s' el %s a las %s.",
                cita.getEmpleado().getNombre(), cita.getCliente().getNombre(),
                cita.getServicio().getNombre(), cita.getFecha(), cita.getHora());
        notificacionService.notificarAdmins(msgAdmin, "COMPLETADA");
    }

    @Override
    @Transactional
    public void cancelarCitaEmpleado(Long citaId, Long empleadoId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita", citaId));

        if (!cita.getEmpleado().getId().equals(empleadoId)) {
            throw new RecursoNoEncontradoException("Cita", citaId);
        }
        if (cita.getEstado() != EstadoCita.PENDIENTE && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new IllegalStateException(
                    String.format("La cita con id=%d tiene estado '%s' y no puede ser cancelada.", citaId, cita.getEstado()));
        }

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setFechaUltimaModificacion(LocalDateTime.now());
        cita.setDetalleUltimoCambio("Cita cancelada por el empleado.");
        citaRepository.save(cita);

        String msgCliente = String.format(
                "Tu cita para '%s' con %s el %s a las %s fue cancelada por el empleado.",
                cita.getServicio().getNombre(), cita.getEmpleado().getNombre(),
                cita.getFecha(), cita.getHora());
        notificacionService.crear(cita.getCliente().getId(), msgCliente, "CANCELACION");

        String msgAdmin = String.format(
                "El empleado %s canceló la cita de %s para '%s' el %s a las %s.",
                cita.getEmpleado().getNombre(), cita.getCliente().getNombre(),
                cita.getServicio().getNombre(), cita.getFecha(), cita.getHora());
        notificacionService.notificarAdmins(msgAdmin, "CANCELACION");
    }

    private boolean haySolapamiento(LocalTime nuevoInicio, LocalTime nuevoFin,
                                    List<Cita> citasExistentes, int duracionMinutos) {
        for (Cita cita : citasExistentes) {
            LocalTime existenteInicio = cita.getHora();
            int durCitaExistente = cita.getServicio() != null
                    ? cita.getServicio().getDuracionMinutos()
                    : duracionMinutos;
            LocalTime existenteFin = existenteInicio.plusMinutes(durCitaExistente);

            if (existenteInicio.isBefore(nuevoFin) && nuevoInicio.isBefore(existenteFin)) {
                return true;
            }
        }
        return false;
    }

    private boolean seSolapa(LocalTime nuevoInicio, LocalTime nuevoFin, Cita cita,
                              int duracionAlternativa) {
        LocalTime finExistente = cita.getHora().plusMinutes(cita.getServicio() != null
                ? cita.getServicio().getDuracionMinutos() : duracionAlternativa);
        return cita.getHora().isBefore(nuevoFin) && nuevoInicio.isBefore(finExistente);
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private Empleado buscarEmpleado(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado", id));
    }

    private Servicio buscarServicio(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
    }
}
