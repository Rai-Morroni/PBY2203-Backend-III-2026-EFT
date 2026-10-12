package com.bancoxyz.bff.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovimientoDiario {
    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
}