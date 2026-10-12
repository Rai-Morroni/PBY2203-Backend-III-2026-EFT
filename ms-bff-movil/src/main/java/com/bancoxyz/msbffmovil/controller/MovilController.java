package com.bancoxyz.msbffmovil.controller;

import com.bancoxyz.msbffmovil.client.CuentasClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movil")
public class MovilController {

    @Autowired
    private CuentasClient cuentasClient;

    @GetMapping("/resumen/{idCuenta}")
    @CircuitBreaker(name = "cuentasCB", fallbackMethod = "fallbackResumenLigero")
    public ResponseEntity<Map<String, Object>> obtenerResumenLigero(@PathVariable String idCuenta) {
        
        // 1. Llamada al microservicio a través de Feign
        String respuestaMsCuentas = cuentasClient.validarCuenta(idCuenta);
        
        // 2. Construcción de la respuesta ligera (BFF)
        Map<String, Object> respuestaLigera = new HashMap<>();
        respuestaLigera.put("cuenta", idCuenta);
        respuestaLigera.put("estado", "ACTIVA");
        respuestaLigera.put("detalle_backend", respuestaMsCuentas);
        
        return ResponseEntity.ok(respuestaLigera);
    }

    // MÉTODO DE RESPALDO (Falla manejada por Resilience4j)
    public ResponseEntity<Map<String, Object>> fallbackResumenLigero(String idCuenta, Throwable e) {
        Map<String, Object> respuestaFallback = new HashMap<>();
        respuestaFallback.put("cuenta", idCuenta);
        respuestaFallback.put("mensaje_amigable", "Servicio de cuentas en mantenimiento. Mostrando datos cacheados o limitados.");
        respuestaFallback.put("error_real", e.getMessage()); // Solo para propósitos académicos de la entrega
        
        return ResponseEntity.status(503).body(respuestaFallback);
    }
}