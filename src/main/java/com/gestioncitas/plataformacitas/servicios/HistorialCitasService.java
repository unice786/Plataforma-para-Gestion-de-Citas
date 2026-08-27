package com.gestioncitas.plataformacitas.servicios;

import com.gestioncitas.plataformacitas.dto.HistorialCitasResponse;
import java.util.List;

public interface HistorialCitasService {

    List<HistorialCitasResponse> listarPorCliente(Long clienteId);
}