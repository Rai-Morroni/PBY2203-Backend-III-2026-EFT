package com.bancoxyz.msbffmovil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign buscará "ms-cuentas" en Eureka automáticamente
@FeignClient(name = "ms-cuentas")
public interface CuentasClient {

    // Debe coincidir con un endpoint real de tu ms-cuentas
    @GetMapping("/api/v1/cuentas/validar/{id}")
    String validarCuenta(@PathVariable("id") String id);
}