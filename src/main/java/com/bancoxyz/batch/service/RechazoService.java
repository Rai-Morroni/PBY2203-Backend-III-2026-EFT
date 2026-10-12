package com.bancoxyz.batch.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RechazoService {

    private final JdbcTemplate jdbcTemplate;

    public RechazoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrar(String jobName, String fileName, int lineNumber, String reason, String content) {
        jdbcTemplate.update("""
                INSERT INTO batch_rechazos
                    (job_nombre, archivo_origen, linea_origen, motivo, contenido_original, fecha_proceso)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    motivo = VALUES(motivo),
                    contenido_original = VALUES(contenido_original),
                    fecha_proceso = CURRENT_TIMESTAMP
                """, jobName, fileName, lineNumber, reason, content);
    }
}
