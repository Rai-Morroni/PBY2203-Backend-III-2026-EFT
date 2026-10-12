package com.bancoxyz.batch.dto;

public interface SourceRecord {

    String getArchivoOrigen();

    void setArchivoOrigen(String archivoOrigen);

    int getLineaOrigen();

    void setLineaOrigen(int lineaOrigen);

    String getContenidoOriginal();

    void setContenidoOriginal(String contenidoOriginal);
}
