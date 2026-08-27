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

    public CitaServiceImpl(CitaRepository citaRepository,
                           ClienteRepository clienteRepository,
                           EmpleadoRepository empleadoRepository,
                           ServicioRepository servicioRepository,
                           HorarioDisponibilidadRepository horarioRepository) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.horarioRepository = horarioRepository;
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

        return new CitaResponseDTO(
                citaGuardada.getId(),
                cliente.getNombre(),
                empleado.getNombre(),
                servicio.getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora(),
                citaGuardada.getEstado(),
                citaGuardada.getFechaRegistro(),
                String.format(
                        "¡Cita reservada exitosamente! Su cita para '%s' con %s está agendada para el %s a las %s.",
                        servicio.getNombre(),
                        empleado.getNombre(),
                        citaGuardada.getFecha(),
                        citaGuardada.getHora()
                )
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
        LocalTime nuevaHoraInicio = request.getHora();
        LocalTime nuevaHoraFin = nuevaHoraInicio.plusMinutes(servicio.getDuracionMinutos());

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
        Cita citaGuardada = citaRepository.save(cita);

        return new CitaResponseDTO(
                citaGuardada.getId(),
                cita.getCliente().getNombre(),
                empleado.getNombre(),
                servicio.getNombre(),
                citaGuardada.getFecha(),
                citaGuardada.getHora(),
                citaGuardada.getEstado(),
                citaGuardada.getFechaRegistro(),
                String.format(
                        "¡Cita reprogramada exitosamente! Su cita para '%s' con %s ha sido movida al %s a las %s.",
                        servicio.getNombre(),
                        empleado.getNombre(),
                        citaGuardada.getFecha(),
                        citaGuardada.getHora()
                )
        );
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
