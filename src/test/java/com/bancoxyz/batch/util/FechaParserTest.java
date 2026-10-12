package com.bancoxyz.batch.util;

import com.bancoxyz.batch.validation.RegistroInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FechaParserTest {

    @Test
    void parsesAllSupportedFormatsStrictly() {
        LocalDate expected = LocalDate.of(2024, 3, 5);
        assertEquals(expected, FechaParser.parse("2024-03-05"));
        assertEquals(expected, FechaParser.parse("2024/03/05"));
        assertEquals(expected, FechaParser.parse("05/03/2024"));
        assertEquals(expected, FechaParser.parse("05-03-2024"));
    }

    @Test
    void rejectsImpossibleEmptyUnknownAndFutureDates() {
        assertInvalid("31/02/2024");
        assertInvalid("");
        assertInvalid("03.05.2024");
        assertInvalid(LocalDate.now().plusDays(1).toString());
    }

    private void assertInvalid(String date) {
        RegistroInvalidoException exception = assertThrows(
                RegistroInvalidoException.class, () -> FechaParser.parse(date));
        assertEquals("FECHA_INVALIDA", exception.getMotivo());
    }
}
