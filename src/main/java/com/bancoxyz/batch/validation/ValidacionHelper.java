package com.bancoxyz.batch.validation;

import com.bancoxyz.batch.util.Normalizacion;

import java.math.BigDecimal;

public final class ValidacionHelper {

    private ValidacionHelper() {
    }

    public static long cuentaPositiva(String valor) {
        try {
            long cuenta = Long.parseLong(valor.trim());
            if (cuenta <= 0) {
                throw new NumberFormatException("Debe ser mayor que cero");
            }
            return cuenta;
        } catch (RuntimeException ex) {
            throw new RegistroInvalidoException("CUENTA_INVALIDA", "Identificador de cuenta inválido: " + valor);
        }
    }

    public static BigDecimal montoMagnitud(String valor) {
        try {
            BigDecimal monto = Normalizacion.monto(valor);
            if (monto.signum() == 0) {
                throw new NumberFormatException("No puede ser cero");
            }
            return monto.abs();
        } catch (RuntimeException ex) {
            throw new RegistroInvalidoException("MONTO_INVALIDO", "Monto vacío, no numérico o cero: " + valor);
        }
    }

    public static BigDecimal saldoPositivo(String valor) {
        try {
            BigDecimal saldo = Normalizacion.monto(valor);
            if (saldo.signum() <= 0) {
                throw new NumberFormatException("Debe ser mayor que cero");
            }
            return saldo;
        } catch (RuntimeException ex) {
            throw new RegistroInvalidoException("MONTO_INVALIDO", "Saldo vacío, no numérico o no positivo: " + valor);
        }
    }
}
