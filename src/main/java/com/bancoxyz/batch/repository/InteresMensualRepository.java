package com.bancoxyz.batch.repository;
import com.bancoxyz.batch.model.InteresMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteresMensualRepository extends JpaRepository<InteresMensual, Long> {}