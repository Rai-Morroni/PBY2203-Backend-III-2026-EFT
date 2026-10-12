package com.bancoxyz.batch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Locale;

public final class Normalizacion {

    private Normalizacion() {
    }

    public static String texto(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    public static BigDecimal monto(String value) {
        return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
    }
}
