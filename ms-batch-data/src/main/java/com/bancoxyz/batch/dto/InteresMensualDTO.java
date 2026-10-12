package com.bancoxyz.batch.dto;

import lombok.Data;

@Data
public class InteresMensualDTO implements SourceRecord {
    private String cuentaId;
    private String nombre;
    private String saldo;
    private String edad;
    private String tipo;
    private String archivoOrigen;
    private int lineaOrigen;
    private String contenidoOriginal;
}