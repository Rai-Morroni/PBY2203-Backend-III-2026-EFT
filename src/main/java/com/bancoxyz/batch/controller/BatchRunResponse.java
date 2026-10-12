package com.bancoxyz.batch.controller;

public record BatchRunResponse(
        String estado,
        Long jobExecutionId,
        long leidos,
        long escritos,
        long filtrados,
        long rechazados,
        long duracionMs,
        String mensaje) {
}
