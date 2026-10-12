package com.bancoxyz.bff.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EstadoAnual {
    private Long id;
    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
}