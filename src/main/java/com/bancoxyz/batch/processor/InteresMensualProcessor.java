package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.InteresMensualDTO;
import com.bancoxyz.batch.model.InteresMensual;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
// Procesador para InteresMensualDTO a InteresMensual
public class InteresMensualProcessor implements ItemProcessor<InteresMensualDTO, InteresMensual> {

    @Override
    public InteresMensual process(InteresMensualDTO dto) throws Exception {
        // Validación de datos corruptos
        if ("-1".equals(dto.getTipo()) || dto.getEdad() == null || dto.getEdad().trim().isEmpty() || "NaN".equalsIgnoreCase(dto.getEdad())) {
            log.warn("Registro descartado por datos corruptos en cuenta ID: {}", dto.getCuentaId());
            return null; // Skip
        }

        InteresMensual entity = new InteresMensual();
try {
            int edad = (int) Double.parseDouble(dto.getEdad());
            if (edad <= 0) return null; 
            
            BigDecimal saldoOriginal = new BigDecimal(dto.getSaldo());
            BigDecimal tasaInteres = BigDecimal.ZERO;

            // Lógica Matemática: Asignación de tasas según el producto
            if ("ahorro".equalsIgnoreCase(dto.getTipo())) {
                tasaInteres = new BigDecimal("0.02"); // 2% a favor del cliente
            } else if ("prestamo".equalsIgnoreCase(dto.getTipo()) || "hipoteca".equalsIgnoreCase(dto.getTipo())) {
                tasaInteres = new BigDecimal("0.05"); // 5% de interés de deuda
            }

            // Cálculo: Saldo Final = Saldo Original + (Saldo Original * Tasa)
            BigDecimal interesCalculado = saldoOriginal.multiply(tasaInteres);
            BigDecimal saldoFinal = saldoOriginal.add(interesCalculado);
            
            entity.setCuentaId(Long.parseLong(dto.getCuentaId()));
            entity.setNombre(dto.getNombre());
            entity.setSaldo(saldoFinal); // Guardamos el saldo con el cálculo matemático aplicado
            entity.setEdad(edad);
            entity.setTipo(dto.getTipo());
        } catch (Exception e) {
            log.error("Error parseando Interes Mensual para cuenta {}: {}", dto.getCuentaId(), e.getMessage());
            return null;
        }
        return entity;
    }
}