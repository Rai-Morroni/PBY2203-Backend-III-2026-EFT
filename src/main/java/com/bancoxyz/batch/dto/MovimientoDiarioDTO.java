package com.bancoxyz.batch.dto;

import lombok.Data;

@Data
public class MovimientoDiarioDTO implements SourceRecord {
    private String cuentaId;
    private String fecha;
    private String tipo;
    private String monto;
    private String archivoOrigen;
    private int lineaOrigen;
    private String contenidoOriginal;
}