package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.MovimientoDiarioDTO;
import com.bancoxyz.batch.model.MovimientosFinancierosDiarios;
import com.bancoxyz.batch.validation.ValidacionHelper;
import com.bancoxyz.batch.util.FechaParser;
import com.bancoxyz.batch.util.Normalizacion;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MovimientoDiarioProcessor implements ItemProcessor<MovimientoDiarioDTO, MovimientosFinancierosDiarios> {

    @Override
    public MovimientosFinancierosDiarios process(MovimientoDiarioDTO dto) {
        String tipo = Normalizacion.texto(dto.getTipo());
        if (!tipo.equals("debito") && !tipo.equals("credito")) {
            throw new RegistroInvalidoException(
                    "TIPO_INVALIDO", "Tipo de movimiento no permitido: " + dto.getTipo());
        }

        BigDecimal amount = ValidacionHelper.montoMagnitud(dto.getMonto());
        MovimientosFinancierosDiarios entity = new MovimientosFinancierosDiarios();
        entity.setCuentaId(ValidacionHelper.cuentaPositiva(dto.getCuentaId()));
        entity.setTipo(tipo);
        entity.setMonto(tipo.equals("debito") ? amount.negate() : amount);
        entity.setFecha(FechaParser.parse(dto.getFecha()));
        entity.setArchivoOrigen(dto.getArchivoOrigen());
        entity.setLineaOrigen(dto.getLineaOrigen());
        return entity;
    }
}