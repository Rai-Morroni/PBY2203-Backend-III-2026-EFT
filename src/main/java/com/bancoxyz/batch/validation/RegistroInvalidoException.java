package com.bancoxyz.batch.validation;

public class RegistroInvalidoException extends RuntimeException {

    private final String motivo;

    public RegistroInvalidoException(String motivo, String message) {
        super(message);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
