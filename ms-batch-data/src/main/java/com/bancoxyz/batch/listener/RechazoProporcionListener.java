package com.bancoxyz.batch.listener;

import com.bancoxyz.batch.config.BatchProperties;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RechazoProporcionListener implements StepExecutionListener {

    private final BatchProperties properties;

    public RechazoProporcionListener(BatchProperties properties) {
        this.properties = properties;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long inputCount = stepExecution.getReadCount() + stepExecution.getReadSkipCount();
        long rejected = stepExecution.getReadSkipCount()
                + stepExecution.getProcessSkipCount()
                + stepExecution.getWriteSkipCount();
        if (inputCount == 0) {
            stepExecution.setStatus(BatchStatus.FAILED);
            ExitStatus failed = ExitStatus.FAILED.addExitDescription("El archivo de entrada no contiene registros");
            stepExecution.setExitStatus(failed);
            return failed;
        }
        BigDecimal percentage = BigDecimal.valueOf(rejected)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(inputCount), 4, RoundingMode.HALF_UP);
        if (percentage.compareTo(properties.getMaxRechazoPct()) > 0) {
            stepExecution.setStatus(BatchStatus.FAILED);
            ExitStatus failed = ExitStatus.FAILED.addExitDescription(
                    "Proporción de rechazos " + percentage + "% supera el máximo "
                            + properties.getMaxRechazoPct() + "%");
            stepExecution.setExitStatus(failed);
            return failed;
        }
        return stepExecution.getExitStatus();
    }
}
