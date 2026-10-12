package com.bancoxyz.batch.listener;

import com.bancoxyz.batch.dto.SourceRecord;
import com.bancoxyz.batch.service.RechazoService;
import com.bancoxyz.batch.validation.RegistroInvalidoException;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

public class RechazoSkipListener<T, S> implements SkipListener<T, S> {

    private final String jobName;
    private final String fileName;
    private final RechazoService rechazoService;

    public RechazoSkipListener(String jobName, String fileName, RechazoService rechazoService) {
        this.jobName = jobName;
        this.fileName = fileName;
        this.rechazoService = rechazoService;
    }

    @Override
    public void onSkipInRead(Throwable throwable) {
        if (throwable instanceof FlatFileParseException parseException) {
            rechazoService.registrar(jobName, fileName, parseException.getLineNumber(),
                    "CSV_MALFORMADO", parseException.getInput());
        }
    }

    @Override
    public void onSkipInProcess(T item, Throwable throwable) {
        if (item instanceof SourceRecord source && throwable instanceof RegistroInvalidoException validation) {
            rechazoService.registrar(jobName, source.getArchivoOrigen(), source.getLineaOrigen(),
                    validation.getMotivo(), source.getContenidoOriginal());
        }
    }

    @Override
    public void onSkipInWrite(S item, Throwable throwable) {
    }
}
