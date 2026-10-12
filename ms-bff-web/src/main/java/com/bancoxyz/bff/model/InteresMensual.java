package com.bancoxyz.bff.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InteresMensual {
    private Long id;
    private Long cuentaId;
    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;
}