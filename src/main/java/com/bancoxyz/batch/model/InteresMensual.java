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

@Data
@Entity
@Table(name = "intereses_mensuales", uniqueConstraints =
        @UniqueConstraint(name = "uk_intereses_origen_periodo",
                columnNames = {"archivo_origen", "linea_origen", "periodo"}))
public class InteresMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;

    @Column(name = "saldo_base", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoBase;

    @Column(name = "tasa_anual", nullable = false, precision = 10, scale = 8)
    private BigDecimal tasaAnual;

    @Column(name = "tasa_mensual", nullable = false, precision = 14, scale = 10)
    private BigDecimal tasaMensual;

    @Column(name = "interes_mensual", nullable = false, precision = 19, scale = 2)
    private BigDecimal interesMensual;

    @Column(name = "saldo_final", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoFinal;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(name = "archivo_origen", nullable = false, length = 120)
    private String archivoOrigen;

    @Column(name = "linea_origen", nullable = false)
    private Integer lineaOrigen;
}
