package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.MovimientoDiarioDTO;
import com.bancoxyz.batch.model.MovimientoDiario;
import com.bancoxyz.batch.processor.MovimientoDiarioProcessor;
import com.bancoxyz.batch.repository.MovimientoDiarioRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MovimientoDiarioJobConfig {

    // 1. LECTOR (Lee el CSV línea por línea y lo mapea al DTO)
    @Bean
    public FlatFileItemReader<MovimientoDiarioDTO> movimientoDiarioReader() {
        return new FlatFileItemReaderBuilder<MovimientoDiarioDTO>()
                .name("movimientoDiarioReader")
                .resource(new ClassPathResource("data/movimientos_financieros_diarios.csv"))
                .linesToSkip(1) // Omite la primera línea de cabeceras (id,fecha,monto,tipo)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(MovimientoDiarioDTO.class)
                .build();
    }

    // 2. ESCRITOR (Toma la Entidad procesada y la guarda en MySQL)
    @Bean
    public RepositoryItemWriter<MovimientoDiario> movimientoDiarioWriter(MovimientoDiarioRepository repository) {
        return new RepositoryItemWriterBuilder<MovimientoDiario>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    // 3. STEP (El paso que agrupa Lector -> Procesador -> Escritor)
    @Bean
    public Step movimientoDiarioStep(JobRepository jobRepository, 
                                     PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<MovimientoDiarioDTO> reader,
                                     MovimientoDiarioProcessor processor,
                                     RepositoryItemWriter<MovimientoDiario> writer) {
        return new StepBuilder("movimientoDiarioStep", jobRepository)
                .<MovimientoDiarioDTO, MovimientoDiario>chunk(100, transactionManager) // Lotes de 100 registros
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant() // Implementación de tolerancia a fallos
                .skip(Exception.class) // Si una línea da error, la salta en lugar de detener todo el Job
                .skipLimit(10) // Límite máximo de errores permitidos en este Job
                .build();
    }

    // 4. JOB (La tarea principal ejecutada)
    @Bean
    public Job movimientoDiarioJob(JobRepository jobRepository, Step movimientoDiarioStep) {
        return new JobBuilder("movimientoDiarioJob", jobRepository)
                .start(movimientoDiarioStep)
                .build();
    }
}