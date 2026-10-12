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
@Table(name = "estados_anuales", uniqueConstraints =
        @UniqueConstraint(name = "uk_estados_origen", columnNames = {"archivo_origen", "linea_origen"}))
public class EstadosFinancierosAnuales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;

    @Column(name = "archivo_origen", nullable = false, length = 120)
    private String archivoOrigen;

    @Column(name = "linea_origen", nullable = false)
    private Integer lineaOrigen;
}
