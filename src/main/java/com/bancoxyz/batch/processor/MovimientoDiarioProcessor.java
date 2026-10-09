package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.MovimientoDiarioDTO;
import com.bancoxyz.batch.model.MovimientoDiario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Component
public class MovimientoDiarioProcessor implements ItemProcessor<MovimientoDiarioDTO, MovimientoDiario> {

    // Maneja los dos formatos de fecha que contiene el CSV
    private static final DateTimeFormatter FORMATTER_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FORMATTER_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public MovimientoDiario process(MovimientoDiarioDTO dto) throws Exception {
        log.info("Procesando registro DTO: {}", dto);

        // 1. Validar Tipo de Transacción
        if ("invalido".equalsIgnoreCase(dto.getTipo())) {
            log.warn("Registro con ID {} descartado: Tipo de transacción inválido.", dto.getId());
            return null; // Retornar null en Spring Batch significa "ignorar este registro" (Skip)
        }

        MovimientoDiario entity = new MovimientoDiario();
        
        // 2. Parseo de ID y Monto con Matemática de Comisiones
        try {
            entity.setId(Long.parseLong(dto.getId()));
            entity.setTipo(dto.getTipo());
            
            BigDecimal montoOriginal = new BigDecimal(dto.getMonto());
            
            // Lógica Matemática: Si es un "retiro", resta $300 por cargo de cajero/sistema
            if ("retiro".equalsIgnoreCase(dto.getTipo())) {
                BigDecimal cargo = new BigDecimal("300.00");
                entity.setMonto(montoOriginal.subtract(cargo));
            } else {
                entity.setMonto(montoOriginal);
            }
        } catch (NumberFormatException e) {
            log.error("Error de formato numérico en el registro ID {}: {}", dto.getId(), e.getMessage());
            return null; 
        }

        // 3. Parseo de Fechas Mixtas
        try {
            if (dto.getFecha().contains("/")) {
                entity.setFecha(LocalDate.parse(dto.getFecha(), FORMATTER_SLASH));
            } else {
                entity.setFecha(LocalDate.parse(dto.getFecha(), FORMATTER_DASH));
            }
        } catch (DateTimeParseException e) {
            log.error("Error parseando la fecha para el registro ID {}: {}", dto.getId(), dto.getFecha());
            return null;
        }

        return entity;
    }
}