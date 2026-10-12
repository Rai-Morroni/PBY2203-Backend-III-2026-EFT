package com.bancoxyz.bff.controller;

import com.bancoxyz.bff.model.EstadoAnual;
import com.bancoxyz.bff.model.InteresMensual;
import com.bancoxyz.bff.model.MovimientoDiario;
import com.bancoxyz.bff.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/web/reportes") // Define la ruta base para todos los endpoints de este controlador
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/movimientos-diarios") // Endpoint para obtener todos los movimientos diarios
    public ResponseEntity<List<MovimientoDiario>> getMovimientosDiarios() {
        List<MovimientoDiario> movimientos = reporteService.obtenerMovimientosDiarios();
        if (movimientos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/estados-anuales") // Endpoint para obtener todos los estados anuales
    public ResponseEntity<List<EstadoAnual>> getEstadosAnuales() {
        List<EstadoAnual> estados = reporteService.obtenerEstadosAnuales();
        if (estados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(estados);
    }

    @GetMapping("/intereses-mensuales") // Endpoint para obtener todos los intereses mensuales
    public ResponseEntity<List<InteresMensual>> getInteresesMensuales() {
        List<InteresMensual> intereses = reporteService.obtenerInteresesMensuales();
        if (intereses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(intereses);
    }
}