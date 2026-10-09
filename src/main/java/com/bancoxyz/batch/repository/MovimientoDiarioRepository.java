package com.bancoxyz.batch.repository;

import com.bancoxyz.batch.model.MovimientoDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoDiarioRepository extends JpaRepository<MovimientoDiario, Long> {
}