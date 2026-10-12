package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.SourceRecord;
import org.springframework.batch.item.file.LineMapper;

public class SourceLineMapper<T extends SourceRecord> implements LineMapper<T> {

    private final LineMapper<T> delegate;
    private final String fileName;

    public SourceLineMapper(LineMapper<T> delegate, String fileName) {
        this.delegate = delegate;
        this.fileName = fileName;
    }

    @Override
    public T mapLine(String line, int lineNumber) throws Exception {
        T item = delegate.mapLine(line, lineNumber);
        item.setArchivoOrigen(fileName);
        item.setLineaOrigen(lineNumber);
        item.setContenidoOriginal(line);
        return item;
    }
}
