package com.bancoxyz.batch.dto;

import lombok.Data;

@Data
public class MovimientoDiarioDTO { // DTO para representar los datos de un movimiento diario
    private String id;
    private String fecha;
    private String monto;
    private String tipo;
}