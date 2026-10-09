package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.EstadoAnualDTO;
import com.bancoxyz.batch.model.EstadoAnual;
import com.bancoxyz.batch.processor.EstadoAnualProcessor;
import com.bancoxyz.batch.repository.EstadoAnualRepository;
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
public class EstadoAnualJobConfig {

    @Bean
    // Configura el lector de archivos CSV para Estado Anual
    public FlatFileItemReader<EstadoAnualDTO> estadoAnualReader() {
        return new FlatFileItemReaderBuilder<EstadoAnualDTO>()
                .name("estadoAnualReader")
                .resource(new ClassPathResource("data/estados_financieros_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion")
                .targetType(EstadoAnualDTO.class)
                .build();
    }

    @Bean
    // Configura el escritor de datos para guardar Estado Anual en la base de datos
    public RepositoryItemWriter<EstadoAnual> estadoAnualWriter(EstadoAnualRepository repository) {
        return new RepositoryItemWriterBuilder<EstadoAnual>().repository(repository).methodName("save").build();
    }

    @Bean
    // Configura el paso del job para procesar Estado Anual
    public Step estadoAnualStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                FlatFileItemReader<EstadoAnualDTO> reader, EstadoAnualProcessor processor,
                                RepositoryItemWriter<EstadoAnual> writer) {
        return new StepBuilder("estadoAnualStep", jobRepository)
                .<EstadoAnualDTO, EstadoAnual>chunk(100, transactionManager)
                .reader(reader).processor(processor).writer(writer)
                .faultTolerant().skip(Exception.class).skipLimit(20).build();
    }

    @Bean
    // Configura el job para procesar Estado Anual
    public Job estadoAnualJob(JobRepository jobRepository, Step estadoAnualStep) {
        return new JobBuilder("estadoAnualJob", jobRepository).start(estadoAnualStep).build();
    }
}