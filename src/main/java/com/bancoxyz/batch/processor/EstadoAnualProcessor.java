package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.EstadoAnualDTO;
import com.bancoxyz.batch.model.EstadosFinancierosAnuales;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import com.bancoxyz.batch.validation.ValidacionHelper;
import com.bancoxyz.batch.util.FechaParser;
import com.bancoxyz.batch.util.Normalizacion;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EstadoAnualProcessor implements ItemProcessor<EstadoAnualDTO, EstadosFinancierosAnuales> {

    @Override
    public EstadosFinancierosAnuales process(EstadoAnualDTO dto) {
        String transaction = Normalizacion.texto(dto.getTransaccion());
        boolean income = transaction.equals("deposito");
        if (!income && !transaction.equals("compra") && !transaction.equals("retiro") && !transaction.equals("pago")) {
            throw new RegistroInvalidoException(
                    "TIPO_INVALIDO", "Transacción no permitida: " + dto.getTransaccion());
        }

        BigDecimal amount = ValidacionHelper.montoMagnitud(dto.getMonto());
        EstadosFinancierosAnuales entity = new EstadosFinancierosAnuales();
        entity.setCuentaId(ValidacionHelper.cuentaPositiva(dto.getCuentaId()));
        entity.setMonto(income ? amount : amount.negate());
        entity.setTransaccion(transaction);
        String description = dto.getDescripcion() == null ? "" : dto.getDescripcion().trim();
        entity.setDescripcion(description.isEmpty() ? "SIN DESCRIPCION" : description);
        entity.setFecha(FechaParser.parse(dto.getFecha()));
        entity.setArchivoOrigen(dto.getArchivoOrigen());
        entity.setLineaOrigen(dto.getLineaOrigen());
        return entity;
    }
}