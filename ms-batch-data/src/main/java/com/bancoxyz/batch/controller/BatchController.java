package com.bancoxyz.batch.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.YearMonth;
import java.util.HexFormat;
import java.util.UUID;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job movimientoDiarioJob;
    private final Job estadoAnualJob;
    private final Job interesMensualJob;

    public BatchController(JobLauncher jobLauncher,
                           @Qualifier("movimientoDiarioJob") Job movimientoDiarioJob,
                           @Qualifier("estadoAnualJob") Job estadoAnualJob,
                           @Qualifier("interesMensualJob") Job interesMensualJob) {
        this.jobLauncher = jobLauncher;
        this.movimientoDiarioJob = movimientoDiarioJob;
        this.estadoAnualJob = estadoAnualJob;
        this.interesMensualJob = interesMensualJob;
    }

    @PostMapping("/movimientos")
    public ResponseEntity<BatchRunResponse> iniciarProcesoMovimientos(
            @RequestParam(defaultValue = "false") boolean forzar) {
        return ejecutar(movimientoDiarioJob, "movimientos_financieros_diarios.csv", null, forzar);
    }

    @PostMapping("/estados-anuales")
    public ResponseEntity<BatchRunResponse> iniciarProcesoEstadosAnuales(
            @RequestParam(defaultValue = "false") boolean forzar) {
        return ejecutar(estadoAnualJob, "estados_financieros_anuales.csv", null, forzar);
    }

    @PostMapping("/intereses")
    public ResponseEntity<BatchRunResponse> iniciarProcesoIntereses(
            @RequestParam(required = false) String periodo,
            @RequestParam(defaultValue = "false") boolean forzar) {
        String periodoEjecucion = periodo == null || periodo.isBlank()
                ? YearMonth.now().toString()
                : periodo.trim();
        try {
            YearMonth.parse(periodoEjecucion);
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(emptyResponse("FAILED", "periodo debe tener formato yyyy-MM"));
        }
        return ejecutar(interesMensualJob, "intereses_trimestrales.csv", periodoEjecucion, forzar);
    }

    private ResponseEntity<BatchRunResponse> ejecutar(Job job, String fileName, String periodo, boolean force) {
        try {
            JobParametersBuilder parameters = new JobParametersBuilder()
                    .addString("archivo", fileName)
                    .addString("sha256", sha256(fileName));
            if (periodo != null) {
                parameters.addString("periodo", periodo);
            }
            if (force) {
                parameters.addString("forzarId", UUID.randomUUID().toString());
            }

            JobExecution execution = jobLauncher.run(job, parameters.toJobParameters());
            BatchRunResponse response = summarize(execution, null);
            if (execution.getStatus() == BatchStatus.FAILED) {
                return ResponseEntity.internalServerError().body(response);
            }
            if (execution.getStatus() == BatchStatus.COMPLETED && response.escritos() == 0) {
                return ResponseEntity.internalServerError().body(summarize(execution,
                        "El job terminó sin escrituras; se considera una ejecución fallida."));
            }
            return ResponseEntity.ok(response);
        } catch (JobInstanceAlreadyCompleteException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(emptyResponse("CONFLICT", "El mismo archivo y parámetros ya fueron procesados; use forzar=true para reejecutar."));
        } catch (JobExecutionAlreadyRunningException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(emptyResponse("CONFLICT", "Ya existe una ejecución en curso para este archivo y parámetros."));
        } catch (JobRestartException | JobParametersInvalidException exception) {
            return ResponseEntity.badRequest().body(emptyResponse("FAILED", exception.getMessage()));
        } catch (IOException | NoSuchAlgorithmException exception) {
            return ResponseEntity.internalServerError().body(emptyResponse("FAILED",
                    "No se pudo calcular la identidad del archivo de entrada: " + exception.getMessage()));
        } catch (org.springframework.batch.core.JobExecutionException exception) {
            return ResponseEntity.internalServerError().body(emptyResponse("FAILED", exception.getMessage()));
        }
    }

    private String sha256(String fileName) throws IOException, NoSuchAlgorithmException {
        byte[] content;
        try (var stream = new ClassPathResource("data/" + fileName).getInputStream()) {
            content = stream.readAllBytes();
        }
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    }

    private BatchRunResponse summarize(JobExecution execution, String message) {
        long read = 0;
        long written = 0;
        long filtered = 0;
        long rejected = 0;
        for (var step : execution.getStepExecutions()) {
            read += step.getReadCount() + step.getReadSkipCount();
            written += step.getWriteCount();
            filtered += step.getFilterCount();
            rejected += step.getReadSkipCount() + step.getProcessSkipCount() + step.getWriteSkipCount();
        }
        long duration = execution.getStartTime() == null || execution.getEndTime() == null
                ? 0
                : Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis();
        String description = message == null ? execution.getAllFailureExceptions().stream()
                .map(Throwable::getMessage).filter(value -> value != null && !value.isBlank())
                .findFirst().orElseGet(() -> execution.getStepExecutions().stream()
                        .map(step -> step.getExitStatus().getExitDescription())
                        .filter(value -> value != null && !value.isBlank())
                        .findFirst().orElse(null)) : message;
        return new BatchRunResponse(execution.getStatus().name(), execution.getId(), read, written,
                filtered, rejected, duration, description);
    }

    private BatchRunResponse emptyResponse(String status, String message) {
        return new BatchRunResponse(status, null, 0, 0, 0, 0, 0, message);
    }
}
