package com.bancoxyz.mscuentas.repository;

import com.bancoxyz.mscuentas.model.MovimientoDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoDiarioRepository extends JpaRepository<MovimientoDiario, Long> {
    
    // Método de Spring Data para buscar todos los movimientos de una cuenta
    List<MovimientoDiario> findByCuentaId(Long cuentaId);
}