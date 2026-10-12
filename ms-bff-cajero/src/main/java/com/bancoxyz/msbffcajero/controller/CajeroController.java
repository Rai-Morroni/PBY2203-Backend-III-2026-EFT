package com.bancoxyz.msbffcajero.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cajero")
public class CajeroController {

    @PostMapping("/retiro")
    public ResponseEntity<Map<String, Object>> procesarRetiroCajero(@RequestParam String idCuenta, @RequestParam double monto) {
        // Lógica orientada a operación crítica
        Map<String, Object> respuestaRetiro = new HashMap<>();
        
        if (monto > 200000) {
            respuestaRetiro.put("exito", false);
            respuestaRetiro.put("mensaje", "Excede límite máximo por transacción en cajero (200,000)");
            return ResponseEntity.badRequest().body(respuestaRetiro);
        }

        respuestaRetiro.put("exito", true);
        respuestaRetiro.put("mensaje", "Retiro autorizado. Por favor retire su dinero.");
        respuestaRetiro.put("montoRetirado", monto);
        respuestaRetiro.put("idTransaccion", "CAJ-993821");
        
        return ResponseEntity.ok(respuestaRetiro);
    }
}