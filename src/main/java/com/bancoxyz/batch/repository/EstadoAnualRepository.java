package com.bancoxyz.batch.repository;
import com.bancoxyz.batch.model.EstadoAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoAnualRepository extends JpaRepository<EstadoAnual, Long> {}