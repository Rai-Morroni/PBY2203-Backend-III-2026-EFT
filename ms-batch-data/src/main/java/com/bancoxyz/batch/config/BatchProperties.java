package com.bancoxyz.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "batch")
public class BatchProperties {

    private int skipLimit = 10000;
    private BigDecimal maxRechazoPct = new BigDecimal("80");
    private Intereses intereses = new Intereses();

    public int getSkipLimit() {
        return skipLimit;
    }

    public void setSkipLimit(int skipLimit) {
        this.skipLimit = skipLimit;
    }

    public BigDecimal getMaxRechazoPct() {
        return maxRechazoPct;
    }

    public void setMaxRechazoPct(BigDecimal maxRechazoPct) {
        this.maxRechazoPct = maxRechazoPct;
    }

    public Intereses getIntereses() {
        return intereses;
    }

    public void setIntereses(Intereses intereses) {
        this.intereses = intereses;
    }

    public static class Intereses {
        private TasasAnuales tasaAnual = new TasasAnuales();

        public TasasAnuales getTasaAnual() {
            return tasaAnual;
        }

        public void setTasaAnual(TasasAnuales tasaAnual) {
            this.tasaAnual = tasaAnual;
        }
    }

    public static class TasasAnuales {
        private BigDecimal ahorro = new BigDecimal("0.02");
        private BigDecimal prestamo = new BigDecimal("0.05");
        private BigDecimal hipoteca = new BigDecimal("0.05");

        public BigDecimal getAhorro() {
            return ahorro;
        }

        public void setAhorro(BigDecimal ahorro) {
            this.ahorro = ahorro;
        }

        public BigDecimal getPrestamo() {
            return prestamo;
        }

        public void setPrestamo(BigDecimal prestamo) {
            this.prestamo = prestamo;
        }

        public BigDecimal getHipoteca() {
            return hipoteca;
        }

        public void setHipoteca(BigDecimal hipoteca) {
            this.hipoteca = hipoteca;
        }
    }
}
