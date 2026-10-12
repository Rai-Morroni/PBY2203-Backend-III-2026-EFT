package com.bancoxyz.batch.util;

import com.bancoxyz.batch.validation.RegistroInvalidoException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Map;
import java.util.regex.Pattern;

public final class FechaParser {

    private static final Pattern ANIO_PRIMERO = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$|^\\d{4}/\\d{2}/\\d{2}$");
    private static final Pattern DIA_PRIMERO = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$|^\\d{2}-\\d{2}-\\d{4}$");
    private static final Map<String, DateTimeFormatter> FORMATTERS = Map.of(
            "yyyy-MM-dd", formatter("uuuu-MM-dd"),
            "yyyy/MM/dd", formatter("uuuu/MM/dd"),
            "dd/MM/yyyy", formatter("dd/MM/uuuu"),
            "dd-MM-yyyy", formatter("dd-MM-uuuu")
    );

    private FechaParser() {
    }

    public static LocalDate parse(String value) {
        if (value == null || value.isBlank()) {
            throw new RegistroInvalidoException("FECHA_INVALIDA", "La fecha está vacía");
        }

        String input = value.trim();
        String formato;
        if (ANIO_PRIMERO.matcher(input).matches()) {
            formato = input.charAt(4) == '-' ? "yyyy-MM-dd" : "yyyy/MM/dd";
        } else if (DIA_PRIMERO.matcher(input).matches()) {
            formato = input.charAt(2) == '/' ? "dd/MM/yyyy" : "dd-MM-yyyy";
        } else {
            throw new RegistroInvalidoException("FECHA_INVALIDA", "Formato de fecha no admitido: " + input);
        }

        final LocalDate date;
        try {
            date = LocalDate.parse(input, FORMATTERS.get(formato));
        } catch (RuntimeException ex) {
            throw new RegistroInvalidoException("FECHA_INVALIDA", "Fecha imposible o inválida: " + input);
        }
        if (date.isAfter(LocalDate.now())) {
            throw new RegistroInvalidoException("FECHA_INVALIDA", "No se aceptan fechas futuras: " + input);
        }
        return date;
    }

    private static DateTimeFormatter formatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);
    }
}
