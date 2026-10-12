package com.bancoxyz.batch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.import=optional:configserver:http://localhost:1",
        "spring.cloud.config.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:batchintegration;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.batch.job.enabled=false",
        "batch.skip-limit=10000",
        "batch.max-rechazo-pct=80"
})
@AutoConfigureMockMvc
class BatchExecutionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sameSourceIsIdempotentAndCanBeForcedWithoutDuplicateRows() throws Exception {
        verifyIdempotency("/api/batch/movimientos", "movimientos_diarios",
                "movimientos_financieros_diarios.csv", null, 470, 530);
        verifyIdempotency("/api/batch/estados-anuales", "estados_anuales",
                "estados_financieros_anuales.csv", null, 836, 164);
        verifyIdempotency("/api/batch/intereses", "intereses_mensuales",
                "intereses_trimestrales.csv", "2026-10", 397, 603);
    }

    private void verifyIdempotency(String endpoint, String table, String file, String period,
                                   long expectedWritten, long expectedRejected) throws Exception {
        var firstRequest = post(endpoint);
        var forceRequest = post(endpoint).param("forzar", "true");
        if (period != null) {
            firstRequest.param("periodo", period);
            forceRequest.param("periodo", period);
        }
        JsonNode first = objectMapper.readTree(mockMvc.perform(firstRequest)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        long firstCount = countForFile(table, file, period);
        assertEquals(1000, first.get("leidos").asLong());
        assertEquals(expectedWritten, first.get("escritos").asLong());
        assertEquals(expectedRejected, first.get("rechazados").asLong());
        assertEquals(first.get("escritos").asLong(), firstCount);

        var duplicateRequest = post(endpoint);
        if (period != null) {
            duplicateRequest.param("periodo", period);
        }
        mockMvc.perform(duplicateRequest).andExpect(status().isConflict());

        JsonNode forced = objectMapper.readTree(mockMvc.perform(forceRequest)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertEquals(firstCount, countForFile(table, file, period));
        assertEquals(first.get("escritos").asLong(), forced.get("escritos").asLong());

        Long rejected = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM batch_rechazos WHERE archivo_origen = ?", Long.class, file);
        assertEquals(first.get("rechazados").asLong(), rejected);
        String duplicatesSql = period == null
                ? "SELECT COUNT(*) FROM (SELECT archivo_origen, linea_origen FROM " + table
                        + " GROUP BY archivo_origen, linea_origen HAVING COUNT(*) > 1) duplicates"
                : "SELECT COUNT(*) FROM (SELECT archivo_origen, linea_origen, periodo FROM " + table
                        + " GROUP BY archivo_origen, linea_origen, periodo HAVING COUNT(*) > 1) duplicates";
        assertEquals(0L, jdbcTemplate.queryForObject(duplicatesSql, Long.class));
    }

    private long countForFile(String table, String file, String period) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE archivo_origen = ?";
        if (period != null) {
            sql += " AND periodo = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, file, period);
        }
        return jdbcTemplate.queryForObject(sql, Long.class, file);
    }
}
