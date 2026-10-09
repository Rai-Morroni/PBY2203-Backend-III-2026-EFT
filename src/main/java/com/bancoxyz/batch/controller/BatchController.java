package com.bancoxyz.batch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("movimientoDiarioJob") // Especifica que se inyecte el Job correcto definido en MovimientoDiarioJobConfig
    private Job movimientoDiarioJob;

    @Autowired
    @Qualifier("estadoAnualJob")
    private Job estadoAnualJob;

    @Autowired
    @Qualifier("interesMensualJob")
    private Job interesMensualJob;

    @PostMapping("/estados-anuales") // Endpoint para iniciar el proceso de Estados Anuales
    public ResponseEntity<String> iniciarProcesoEstadosAnuales() {
        try {
            // Se añade un parámetro de tiempo para que Spring Batch reconozca cada ejecución como única
            jobLauncher.run(estadoAnualJob, new JobParametersBuilder().addLong("tiempoInicio", System.currentTimeMillis()).toJobParameters());
            return ResponseEntity.ok("Proceso Batch de Estados Anuales ejecutado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/intereses") // Endpoint para iniciar el proceso de Intereses Mensuales
    public ResponseEntity<String> iniciarProcesoIntereses() {
        try {
            // Se añade un parámetro de tiempo para que Spring Batch reconozca cada ejecución como única
            jobLauncher.run(interesMensualJob, new JobParametersBuilder().addLong("tiempoInicio", System.currentTimeMillis()).toJobParameters());
            return ResponseEntity.ok("Proceso Batch de Intereses Mensuales ejecutado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/movimientos") // Endpoint para iniciar el proceso de Movimientos Diarios
    public ResponseEntity<String> iniciarProcesoMovimientos() {
        try {
            // Se añade un parámetro de tiempo para que Spring Batch reconozca cada ejecución como única
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("tiempoInicio", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(movimientoDiarioJob, jobParameters);
            
            return ResponseEntity.ok("Proceso Batch de Movimientos Diarios ejecutado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al ejecutar el proceso: " + e.getMessage());
        }
    }
}