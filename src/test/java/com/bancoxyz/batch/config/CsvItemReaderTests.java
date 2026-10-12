package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.EstadoAnualDTO;
import com.bancoxyz.batch.dto.InteresMensualDTO;
import com.bancoxyz.batch.dto.MovimientoDiarioDTO;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvItemReaderTests {

    @Test
    void readsEstadoAnualCsvIntoDto() throws Exception {
        var reader = new EstadoAnualJobConfig().estadoAnualReader();
        reader.open(new ExecutionContext());
        try {
            EstadoAnualDTO item = reader.read();

            assertEquals("104", item.getCuentaId());
            assertEquals("2024/03/22", item.getFecha());
            assertEquals("compra", item.getTransaccion());
        } finally {
            reader.close();
        }
    }

    @Test
    void readsInteresTrimestralCsvIntoDto() throws Exception {
        var reader = new InteresMensualJobConfig().interesMensualReader();
        reader.open(new ExecutionContext());
        try {
            InteresMensualDTO item = reader.read();

            assertEquals("131", item.getCuentaId());
            assertEquals("Diana Prince", item.getNombre());
            assertEquals("8000", item.getSaldo());
        } finally {
            reader.close();
        }
    }

    @Test
    void readsMovimientoDiarioCsvIntoDto() throws Exception {
        var reader = new MovimientoDiarioJobConfig().movimientoDiarioReader();
        reader.open(new ExecutionContext());
        try {
            MovimientoDiarioDTO item = reader.read();

            assertEquals("1", item.getCuentaId());
            assertEquals("2024/03/05", item.getFecha());
            assertEquals("invalido", item.getTipo());
            assertEquals("1500", item.getMonto());
            assertEquals("movimientos_financieros_diarios.csv", item.getArchivoOrigen());
            assertEquals(2, item.getLineaOrigen());
        } finally {
            reader.close();
        }
    }
}
