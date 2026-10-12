package com.bancoxyz.batch.dto;

import lombok.Data;

@Data
public class EstadoAnualDTO implements SourceRecord {
    private String cuentaId;
    private String fecha;
    private String transaccion;
    private String monto;
    private String descripcion;
    private String archivoOrigen;
    private int lineaOrigen;
    private String contenidoOriginal;
}