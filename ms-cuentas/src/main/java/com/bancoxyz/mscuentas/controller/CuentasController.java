package com.bancoxyz.mscuentas.controller;

import com.bancoxyz.mscuentas.model.MovimientoDiario;
import com.bancoxyz.mscuentas.repository.MovimientoDiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
public class CuentasController {

    @Autowired
    private MovimientoDiarioRepository repository;

    @GetMapping("/validar/{id}")
    public String validarCuenta(@PathVariable("id") Long id) {
        
        // 1. Ir a la base de datos y buscar los registros reales del CSV
        List<MovimientoDiario> movimientos = repository.findByCuentaId(id);
        
        if (movimientos.isEmpty()) {
            return "Cuenta " + id + " activa, pero sin movimientos registrados.";
        }

        // 2. Lógica de negocio real: Sumar los montos para obtener el saldo
        BigDecimal saldoTotal = movimientos.stream()
                .map(MovimientoDiario::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Devolver el dato crudo al BFF
        return "Cuenta " + id + " validada. Movimientos históricos: " + movimientos.size() + 
               ". Saldo actual calculado: $" + saldoTotal;
    }
}