package com.bancoxyz.batch.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "estados_anuales")
public class EstadoAnual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Se genera automáticamente un ID único para cada registro
    private Long id;
    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
}