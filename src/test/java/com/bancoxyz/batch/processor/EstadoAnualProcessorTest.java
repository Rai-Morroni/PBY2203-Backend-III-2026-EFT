package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.dto.EstadoAnualDTO;
import com.bancoxyz.batch.model.EstadosFinancierosAnuales;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadoAnualProcessorTest {

    private final EstadoAnualProcessor processor = new EstadoAnualProcessor();

    @Test
    void normalizesDepositAndAcceptsMissingDescription() {
        EstadoAnualDTO dto = item("depósito", "100.005");
        EstadosFinancierosAnuales result = processor.process(dto);
        assertEquals("deposito", result.getTransaccion());
        assertEquals(new BigDecimal("100.01"), result.getMonto());
        assertEquals("SIN DESCRIPCION", result.getDescripcion());
    }

    @Test
    void normalizesExpensesToNegativeAndRejectsInvalidBusinessFields() {
        assertEquals(new BigDecimal("-25.00"), processor.process(item("compra", "-25")).getMonto());
        assertInvalid(item("devolucion", "25"), "TIPO_INVALIDO");
        assertInvalid(item("pago", ""), "MONTO_INVALIDO");
        assertInvalid(item("retiro", "0"), "MONTO_INVALIDO");
        EstadoAnualDTO invalidAccount = item("compra", "5");
        invalidAccount.setCuentaId("abc");
        assertInvalid(invalidAccount, "CUENTA_INVALIDA");
        EstadoAnualDTO invalidDate = item("compra", "5");
        invalidDate.setFecha("31/02/2024");
        assertInvalid(invalidDate, "FECHA_INVALIDA");
    }

    private EstadoAnualDTO item(String transaction, String amount) {
        EstadoAnualDTO dto = new EstadoAnualDTO();
        dto.setCuentaId("104");
        dto.setFecha("22/03/2024");
        dto.setTransaccion(transaction);
        dto.setMonto(amount);
        dto.setDescripcion("  ");
        dto.setArchivoOrigen("estados_financieros_anuales.csv");
        dto.setLineaOrigen(2);
        dto.setContenidoOriginal("104,22/03/2024," + transaction + "," + amount + ",");
        return dto;
    }

    private void assertInvalid(EstadoAnualDTO dto, String reason) {
        RegistroInvalidoException exception = assertThrows(
                RegistroInvalidoException.class, () -> processor.process(dto));
        assertEquals(reason, exception.getMotivo());
    }
}
