package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.MovimientoDiarioDTO;
import com.bancoxyz.batch.model.MovimientosFinancierosDiarios;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimientoDiarioProcessorTest {

    private final MovimientoDiarioProcessor processor = new MovimientoDiarioProcessor();

    @Test
    void normalizesSignsByMovementTypeAndSourceMetadata() {
        MovimientoDiarioDTO debit = item("debito", "1500.00");
        assertEquals(new BigDecimal("-1500.00"), processor.process(debit).getMonto());

        MovimientoDiarioDTO credit = item("credito", "-200.50");
        MovimientosFinancierosDiarios result = processor.process(credit);
        assertEquals(new BigDecimal("200.50"), result.getMonto());
        assertEquals("movimientos_financieros_diarios.csv", result.getArchivoOrigen());
        assertEquals(2, result.getLineaOrigen());
    }

    @Test
    void rejectsUnsupportedTypesZeroAndInvalidAmountsOrDates() {
        assertInvalid(item("invalido", "10"), "TIPO_INVALIDO");
        assertInvalid(item("desconocido", "10"), "TIPO_INVALIDO");
        assertInvalid(item("credito", ""), "MONTO_INVALIDO");
        assertInvalid(item("debito", "no-numero"), "MONTO_INVALIDO");
        assertInvalid(item("debito", "0"), "MONTO_INVALIDO");
        MovimientoDiarioDTO invalidDate = item("credito", "1");
        invalidDate.setFecha("31/02/2024");
        assertInvalid(invalidDate, "FECHA_INVALIDA");
        MovimientoDiarioDTO invalidAccount = item("credito", "1");
        invalidAccount.setCuentaId("0");
        assertInvalid(invalidAccount, "CUENTA_INVALIDA");
    }

    private MovimientoDiarioDTO item(String type, String amount) {
        MovimientoDiarioDTO dto = new MovimientoDiarioDTO();
        dto.setCuentaId("2");
        dto.setFecha("2024/03/05");
        dto.setTipo(type);
        dto.setMonto(amount);
        dto.setArchivoOrigen("movimientos_financieros_diarios.csv");
        dto.setLineaOrigen(2);
        dto.setContenidoOriginal("2,2024/03/05," + type + "," + amount);
        return dto;
    }

    private void assertInvalid(MovimientoDiarioDTO dto, String reason) {
        RegistroInvalidoException exception = assertThrows(
                RegistroInvalidoException.class, () -> processor.process(dto));
        assertEquals(reason, exception.getMotivo());
    }
}
