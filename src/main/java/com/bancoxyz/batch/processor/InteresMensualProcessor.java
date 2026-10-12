package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.InteresMensualDTO;
import com.bancoxyz.batch.model.InteresMensual;
import com.bancoxyz.batch.config.BatchProperties;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import com.bancoxyz.batch.validation.ValidacionHelper;
import com.bancoxyz.batch.util.Normalizacion;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

public class InteresMensualProcessor implements ItemProcessor<InteresMensualDTO, InteresMensual> {

    private final BatchProperties properties;
    private final YearMonth periodo;

    public InteresMensualProcessor(BatchProperties properties, YearMonth periodo) {
        this.properties = properties;
        this.periodo = periodo;
    }

    @Override
    public InteresMensual process(InteresMensualDTO dto) {
        String tipo = Normalizacion.texto(dto.getTipo());
        BigDecimal tasaAnual = switch (tipo) {
            case "ahorro" -> properties.getIntereses().getTasaAnual().getAhorro();
            case "prestamo" -> properties.getIntereses().getTasaAnual().getPrestamo();
            case "hipoteca" -> properties.getIntereses().getTasaAnual().getHipoteca();
            default -> throw new RegistroInvalidoException("TIPO_INVALIDO", "Tipo de producto no permitido: " + dto.getTipo());
        };
        String nombre = dto.getNombre() == null ? "" : dto.getNombre().trim();
        if (nombre.isEmpty() || Normalizacion.texto(nombre).equals("unknown")) {
            throw new RegistroInvalidoException("NOMBRE_INVALIDO", "El nombre está vacío o es unknown");
        }
        int edad;
        try {
            if (dto.getEdad() == null || !dto.getEdad().trim().matches("\\d+")) {
                throw new NumberFormatException("Edad no entera");
            }
            edad = Integer.parseInt(dto.getEdad().trim());
        } catch (NumberFormatException ex) {
            throw new RegistroInvalidoException("EDAD_FUERA_DE_RANGO", "La edad debe ser entera entre 18 y 120");
        }
        if (edad < 18 || edad > 120) {
            throw new RegistroInvalidoException("EDAD_FUERA_DE_RANGO", "La edad debe estar entre 18 y 120");
        }

        BigDecimal saldoBase = ValidacionHelper.saldoPositivo(dto.getSaldo());
        InteresMensual entity = new InteresMensual();
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal interesMensual = calcularInteresMensual(saldoBase, tasaAnual);
        entity.setCuentaId(ValidacionHelper.cuentaPositiva(dto.getCuentaId()));
        entity.setNombre(nombre);
        entity.setSaldo(saldoBase.add(interesMensual));
        entity.setSaldoBase(saldoBase);
        entity.setTasaAnual(tasaAnual);
        entity.setTasaMensual(tasaMensual);
        entity.setInteresMensual(interesMensual);
        entity.setSaldoFinal(saldoBase.add(interesMensual));
        entity.setPeriodo(periodo.toString());
        entity.setEdad(edad);
        entity.setTipo(tipo);
        entity.setArchivoOrigen(dto.getArchivoOrigen());
        entity.setLineaOrigen(dto.getLineaOrigen());
        return entity;
    }

    private BigDecimal calcularInteresMensual(BigDecimal saldoBase, BigDecimal tasaAnual) {
        return saldoBase.multiply(tasaAnual)
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }
}