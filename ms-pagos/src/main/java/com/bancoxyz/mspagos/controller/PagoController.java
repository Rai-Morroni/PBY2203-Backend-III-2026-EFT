package com.bancoxyz.mspagos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagos") // Endpoint base para el microservicio de pagos
public class PagoController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/procesar/{monto}")
    public ResponseEntity<String> procesarPago(@PathVariable String monto) {
        // Simulamos la lógica de pago
        String mensaje = "Se ha procesado un pago exitoso por el monto de $" + monto;
        
        // Enviamos el mensaje asíncrono a Kafka al tópico "pagos-topic"
        kafkaTemplate.send("pagos-topic", mensaje);
        
        return ResponseEntity.ok("Pago procesado rápidamente. Notificación asíncrona enviada a clientes.");
    }
}