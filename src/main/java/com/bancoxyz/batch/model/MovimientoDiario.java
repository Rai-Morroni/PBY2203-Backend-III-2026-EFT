package com.bancoxyz.batch.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "movimientos_diarios")
public class MovimientoDiario {
    @Id
    private Long id; // El ID viene en el CSV, se usa como llave primaria
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
}