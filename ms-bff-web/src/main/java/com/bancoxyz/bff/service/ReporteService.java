package com.bancoxyz.bff.service;

import com.bancoxyz.bff.model.EstadoAnual;
import com.bancoxyz.bff.model.InteresMensual;
import com.bancoxyz.bff.model.MovimientoDiario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteService { // Servicio para manejar la lógica de negocio relacionada con los reportes

    public List<MovimientoDiario> obtenerMovimientosDiarios() {
        return List.of();
    }

    public List<EstadoAnual> obtenerEstadosAnuales() {
        return List.of();
    }

    public List<InteresMensual> obtenerInteresesMensuales() {
        return List.of();
    }
}