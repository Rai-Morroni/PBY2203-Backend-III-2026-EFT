package com.bancoxyz.msclientes.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PagoListener { // Clase que escucha los eventos de Kafka relacionados con pagos y actualiza el historial del cliente en consecuencia.

    @KafkaListener(topics = "pagos-topic", groupId = "clientes-group")
    public void escucharPago(String mensaje) {
        System.out.println("=================================================");
        System.out.println("EVENTO KAFKA RECIBIDO EN MS-CLIENTES");
        System.out.println("Acción: Actualizando historial del cliente...");
        System.out.println("Detalle: " + mensaje);
        System.out.println("=================================================");
    }
}