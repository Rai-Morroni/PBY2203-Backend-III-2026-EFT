package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.EstadoAnualDTO;
import com.bancoxyz.batch.model.EstadoAnual;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class EstadoAnualProcessor implements ItemProcessor<EstadoAnualDTO, EstadoAnual> {

    // Define formateadores de fecha para diferentes formatos
    private static final DateTimeFormatter FORMATTER_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FORMATTER_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public EstadoAnual process(EstadoAnualDTO dto) throws Exception {
        EstadoAnual entity = new EstadoAnual();
        
        // Manejo de excepciones para datos inválidos
        try {
            entity.setCuentaId(Long.parseLong(dto.getCuentaId()));
            entity.setMonto(new BigDecimal(dto.getMonto()));
            entity.setTransaccion(dto.getTransaccion());
            entity.setDescripcion(dto.getDescripcion());
            
            if (dto.getFecha().contains("/")) {
                entity.setFecha(LocalDate.parse(dto.getFecha(), FORMATTER_SLASH));
            } else {
                entity.setFecha(LocalDate.parse(dto.getFecha(), FORMATTER_DASH));
            }
        } catch (Exception e) {
            log.error("Error procesando Estado Anual para cuenta {}: {}", dto.getCuentaId(), e.getMessage());
            return null; // Skip registro inválido
        }
        return entity;
    }
}