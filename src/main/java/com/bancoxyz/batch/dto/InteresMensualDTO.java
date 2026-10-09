package com.bancoxyz.batch.dto;

import lombok.Data;

@Data
public class InteresMensualDTO { // DTO para representar los datos de un interés mensual
    private String cuentaId;
    private String nombre;
    private String saldo;
    private String edad;
    private String tipo;
}