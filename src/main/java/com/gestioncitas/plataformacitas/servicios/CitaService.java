package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.dto.CitaClienteResponseDTO;
import com.gestioncitas.plataformacitas.dto.CitaResponseDTO;
import com.gestioncitas.plataformacitas.dto.EdicionCitaRequestDTO;
import com.gestioncitas.plataformacitas.dto.HorarioDisponibleDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para la gestión de citas (SCRUM-1).
 *
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Consultar bloques de disponibilidad libres por servicio y fecha.</li>
 *   <li>Registrar una nueva cita validando el anti-double booking.</li>
 * </ul>
 *
 * <h3>Estrategia anti-double booking:</h3>
 * <p>Se cargan todas las citas activas del empleado en la fecha solicitada
 * mediante {@code CitaRepository}. El solapamiento se evalúa en Java comparando
 * intervalos {@code [hora, horaFin)} de cada cita existente con el intervalo
 * de la nueva cita. Esto garantiza portabilidad total entre H2 y MySQL.</p>
 *
 * <p>El método {@link #reservarCita} está anotado con {@code @Transactional}
 * para que la validación y el INSERT ocurran en la misma transacción de base de
 * datos, evitando condiciones de carrera en entornos concurrentes.</p>
 */
@Service
@Transactional(readOnly = true)
public class CitaService {

    private static final List<EstadoCita> ESTADOS_ACTIVOS = List.of(
            EstadoCita.PENDIENTE,
            EstadoCita.CONFIRMADA
    );

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioRepository servicioRepository;
    private final HorarioDisponibilidadRepository horarioRepository;

    // ── Constructor (inyección por constructor) ───────────────────────────

    public CitaService(CitaRepository citaRepository,
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

    // ══════════════════════════════════════════════════════════════════════
    // CONSULTA DE DISPONIBILIDAD
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Consulta los bloques horarios libres para un servicio en una fecha.
     *
     * <p>Algoritmo:
     * <ol>
     *   <li>Obtiene todos los bloques de horario DISPONIBLE de empleados que
     *       ofrecen el servicio (puede haber varios empleados).</li>
     *   <li>Para cada bloque, carga las citas activas del empleado en esa fecha.</li>
     *   <li>Divide el bloque en sub-slots de {@code duracionMinutos} y devuelve
     *       únicamente los que no se solapan con ninguna cita existente.</li>
     * </ol>
     *
     * @param servicioId ID del servicio
     * @param fecha      Fecha a consultar
     * @return Lista de {@link HorarioDisponibleDTO} con los slots libres
     * @throws RecursoNoEncontradoException si el servicio no existe
     */
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

            // Generar sub-slots dentro del bloque de disponibilidad
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

    /**
     * Versión del endpoint con rango de fechas. Agrega disponibilidad de cada
     * día en el rango [desde, hasta].
     *
     * @param servicioId ID del servicio
     * @param desde      Fecha de inicio (inclusive)
     * @param hasta      Fecha de fin (inclusive)
     * @return Lista acumulada de slots libres en el rango
     */
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

    /**
     * Obtiene las citas de un cliente ordenadas cronológicamente. El ID recibido
     * debe provenir de la sesión autenticada y nunca de un parámetro del cliente.
     */
    public List<CitaClienteResponseDTO> listarCitasDelCliente(Long clienteId) {
        return citaRepository.findByClienteIdOrderByFechaAscHoraAsc(clienteId).stream()
                .map(cita -> new CitaClienteResponseDTO(
                        cita.getId(),
                        cita.getFecha(),
                        cita.getHora(),
                        cita.getServicio().getNombre(),
                        cita.getEstado()
                ))
                .toList();
    }

    // ══════════════════════════════════════════════════════════════════════
    // RESERVAR CITA
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Registra una nueva cita tras validar disponibilidad (anti-double booking).
     *
     * @param request DTO de solicitud de reserva validado por Bean Validation
     * @return {@link CitaResponseDTO} con los datos de la cita confirmada
     * @throws RecursoNoEncontradoException  si cliente, empleado o servicio no existen
     * @throws HorarioNoDisponibleException  si el horario ya está ocupado (HTTP 409)
     */
    @Transactional
    public CitaResponseDTO reservarCita(ReservaCitaRequestDTO request) {

        // 1. Validar existencia de entidades relacionadas
        Cliente cliente = buscarCliente(request.getClienteId());
        Empleado empleado = buscarEmpleado(request.getEmpleadoId());
        Servicio servicio = buscarServicio(request.getServicioId());

        // 2. Calcular intervalo de la nueva cita
        LocalTime horaInicio = request.getHora();
        LocalTime horaFin = horaInicio.plusMinutes(servicio.getDuracionMinutos());

        // 3. Cargar citas activas del empleado en esa fecha para verificar solapamiento
        List<Cita> citasExistentes = citaRepository.findCitasActivasByEmpleadoAndFecha(
                empleado.getId(), request.getFecha(), ESTADOS_ACTIVOS);

        // 4. Verificar solapamiento (anti-double booking)
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

        // 5. Crear y persistir la nueva cita
        Cita nuevaCita = new Cita();
        nuevaCita.setCliente(cliente);
        nuevaCita.setEmpleado(empleado);
        nuevaCita.setServicio(servicio);
        nuevaCita.setFecha(request.getFecha());
        nuevaCita.setHora(horaInicio);
        nuevaCita.setEstado(EstadoCita.PENDIENTE);
        nuevaCita.setFechaRegistro(LocalDateTime.now());

        Cita citaGuardada = citaRepository.save(nuevaCita);

        // 6. Construir y retornar respuesta
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

    /** Modifica una cita existente y evita que el nuevo horario se solape con otra cita activa. */
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

    /** Conserva la cita como historial y cambia su estado a CANCELADA. */
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

    // ══════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES PRIVADOS
    // ══════════════════════════════════════════════════════════════════════

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
