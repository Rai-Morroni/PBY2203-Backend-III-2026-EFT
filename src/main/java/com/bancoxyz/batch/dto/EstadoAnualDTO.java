package com.bancoxyz.batch.dto;

import lombok.Data;

@Data
public class EstadoAnualDTO { // DTO para representar los datos de un estado anual
    private String cuentaId;
    private String fecha;
    private String transaccion;
    private String monto;
    private String descripcion;
}