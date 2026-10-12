package com.bancoxyz.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "batch_rechazos", uniqueConstraints =
        @UniqueConstraint(name = "uk_batch_rechazo_origen",
                columnNames = {"job_nombre", "archivo_origen", "linea_origen"}))
public class BatchRechazo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_nombre", nullable = false, length = 100)
    private String jobNombre;

    @Column(name = "archivo_origen", nullable = false, length = 120)
    private String archivoOrigen;

    @Column(name = "linea_origen", nullable = false)
    private Integer lineaOrigen;

    @Column(nullable = false, length = 80)
    private String motivo;

    @Column(name = "contenido_original", columnDefinition = "TEXT")
    private String contenidoOriginal;

    @Column(name = "fecha_proceso", nullable = false)
    private LocalDateTime fechaProceso;
}
