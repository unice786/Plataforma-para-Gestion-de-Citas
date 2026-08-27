package com.gestioncitas.plataformacitas.servicios.impl;

import com.gestioncitas.plataformacitas.dto.HistorialCitasResponse;
import com.gestioncitas.plataformacitas.repositorios.CitaRepository;
import com.gestioncitas.plataformacitas.servicios.HistorialCitasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialCitasServiceImpl implements HistorialCitasService {

    private final CitaRepository citaRepository;

    @Override
    @Transactional
    public List<HistorialCitasResponse> listarPorCliente(Long clienteId) {
        return citaRepository.findByClienteIdOrderByFechaDescHoraDesc(clienteId).stream()
                .map(cita -> new HistorialCitasResponse(
                        cita.getId(),
                        cita.getFecha(),
                        cita.getHora(),
                        cita.getServicio() != null ? cita.getServicio().getNombre() : null,
                        cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : null,
                        cita.getEstado() != null ? cita.getEstado().name() : "PENDIENTE"
                ))
                .collect(Collectors.toList());
    }
}