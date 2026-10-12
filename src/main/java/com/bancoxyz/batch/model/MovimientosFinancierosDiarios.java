package com.bancoxyz.batch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "movimientos_diarios", uniqueConstraints =
        @UniqueConstraint(name = "uk_movimientos_origen", columnNames = {"archivo_origen", "linea_origen"}))
public class MovimientosFinancierosDiarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;

    @Column(name = "archivo_origen", nullable = false, length = 120)
    private String archivoOrigen;

    @Column(name = "linea_origen", nullable = false)
    private Integer lineaOrigen;
}
