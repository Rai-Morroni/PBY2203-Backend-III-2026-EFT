package com.bancoxyz.bff.controller;

import com.bancoxyz.bff.model.MovimientoDiario;
import com.bancoxyz.bff.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(
        controllers = ReporteController.class,
        properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9000/oauth2/jwks"
)
public class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Simula el servicio para no tocar la base de datos real
    @MockBean
    private ReporteService reporteService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    public void testGetMovimientosDiarios_RetornaListaYStatus200() throws Exception {
        // 1. Arrange (Preparar): Creamos datos simulados
        MovimientoDiario mov = new MovimientoDiario();
        mov.setId(1L);
        mov.setFecha(LocalDate.of(2024, 10, 9));
        mov.setMonto(new BigDecimal("1500.00"));
        mov.setTipo("credito");

        // Le indica a Mockito qué responder cuando el controlador llame al servicio
        Mockito.when(reporteService.obtenerMovimientosDiarios()).thenReturn(Arrays.asList(mov));

        // 2 & 3. Act & Assert (Ejecutar y Validar): Simula la petición GET
        mockMvc.perform(get("/api/v1/web/reportes/movimientos-diarios")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andExpect(jsonPath("$[0].id").value(1)) // Valida que el JSON tenga el ID 1
                .andExpect(jsonPath("$[0].tipo").value("credito")); // Valida el tipo
    }

    @Test
    public void testGetEstadosAnuales_RetornaNoContent204_CuandoEstaVacio() throws Exception {
        // 1. Arrange: Simula que la base de datos está vacía
        Mockito.when(reporteService.obtenerEstadosAnuales()).thenReturn(Collections.emptyList());

        // 2 & 3. Act & Assert: Valida que la lógica del controlador devuelva 204 No Content
        mockMvc.perform(get("/api/v1/web/reportes/estados-anuales")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // Espera un 204 No Content
    }
}