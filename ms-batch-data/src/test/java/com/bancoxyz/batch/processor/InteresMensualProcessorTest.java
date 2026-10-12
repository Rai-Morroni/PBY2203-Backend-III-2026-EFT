package com.bancoxyz.batch.processor;

import com.bancoxyz.batch.config.BatchProperties;
import com.bancoxyz.batch.dto.InteresMensualDTO;
import com.bancoxyz.batch.model.InteresMensual;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InteresMensualProcessorTest {

    private final InteresMensualProcessor processor =
            new InteresMensualProcessor(new BatchProperties(), YearMonth.of(2026, 10));

    @Test
    void calculatesMonthlySavingsAndLoanInterestWithHalfUpRounding() {
        InteresMensual savings = processor.process(item("ahorro", "10000", "35", "Ana"));
        assertEquals(new BigDecimal("16.67"), savings.getInteresMensual());
        assertEquals(new BigDecimal("10016.67"), savings.getSaldoFinal());
        assertEquals("2026-10", savings.getPeriodo());

        InteresMensual loan = processor.process(item("prestamo", "8000", "100", "Luis"));
        assertEquals(new BigDecimal("33.33"), loan.getInteresMensual());
        assertEquals(new BigDecimal("8033.33"), loan.getSaldoFinal());
    }

    @Test
    void validatesProductAgeBalanceAndName() {
        for (String type : new String[]{"unknown", "-1", "otro"}) {
            assertInvalid(item(type, "100", "35", "Ana"), "TIPO_INVALIDO");
        }
        assertInvalid(item("ahorro", "100", "", "Ana"), "EDAD_FUERA_DE_RANGO");
        assertInvalid(item("ahorro", "100", "17", "Ana"), "EDAD_FUERA_DE_RANGO");
        assertInvalid(item("ahorro", "100", "150", "Ana"), "EDAD_FUERA_DE_RANGO");
        assertInvalid(item("ahorro", "100", "35.5", "Ana"), "EDAD_FUERA_DE_RANGO");
        assertInvalid(item("ahorro", "", "35", "Ana"), "MONTO_INVALIDO");
        assertInvalid(item("ahorro", "0", "35", "Ana"), "MONTO_INVALIDO");
        assertInvalid(item("ahorro", "100", "35", ""), "NOMBRE_INVALIDO");
        assertInvalid(item("ahorro", "100", "35", "Unknown"), "NOMBRE_INVALIDO");
        assertInvalid(item("ahorro", "-100", "35", "Ana"), "MONTO_INVALIDO");
        InteresMensualDTO invalidAccount = item("ahorro", "100", "35", "Ana");
        invalidAccount.setCuentaId("0");
        assertInvalid(invalidAccount, "CUENTA_INVALIDA");
        assertEquals(120, processor.process(item("hipoteca", "1", "120", "Luis")).getEdad());
    }

    private InteresMensualDTO item(String type, String balance, String age, String name) {
        InteresMensualDTO dto = new InteresMensualDTO();
        dto.setCuentaId("131");
        dto.setNombre(name);
        dto.setSaldo(balance);
        dto.setEdad(age);
        dto.setTipo(type);
        dto.setArchivoOrigen("intereses_trimestrales.csv");
        dto.setLineaOrigen(2);
        dto.setContenidoOriginal("131," + name + "," + balance + "," + age + "," + type);
        return dto;
    }

    private void assertInvalid(InteresMensualDTO dto, String reason) {
        RegistroInvalidoException exception = assertThrows(
                RegistroInvalidoException.class, () -> processor.process(dto));
        assertEquals(reason, exception.getMotivo());
    }
}
