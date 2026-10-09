package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.InteresMensualDTO;
import com.bancoxyz.batch.model.InteresMensual;
import com.bancoxyz.batch.processor.InteresMensualProcessor;
import com.bancoxyz.batch.repository.InteresMensualRepository;
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
public class InteresMensualJobConfig {

    @Bean
    // Configura el lector de archivos CSV para Interés Mensual
    public FlatFileItemReader<InteresMensualDTO> interesMensualReader() {
        return new FlatFileItemReaderBuilder<InteresMensualDTO>()
                .name("interesMensualReader")
                .resource(new ClassPathResource("data/intereses_trimestrales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
                .targetType(InteresMensualDTO.class)
                .build();
    }

    @Bean
    // Configura el escritor de datos para guardar Interés Mensual en la base de datos
    public RepositoryItemWriter<InteresMensual> interesMensualWriter(InteresMensualRepository repository) {
        return new RepositoryItemWriterBuilder<InteresMensual>().repository(repository).methodName("save").build();
    }

    @Bean
    // Configura el paso del job para procesar Interés Mensual
    public Step interesMensualStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<InteresMensualDTO> reader, InteresMensualProcessor processor,
                                   RepositoryItemWriter<InteresMensual> writer) {
        return new StepBuilder("interesMensualStep", jobRepository)
                .<InteresMensualDTO, InteresMensual>chunk(100, transactionManager)
                .reader(reader).processor(processor).writer(writer)
                .faultTolerant().skip(Exception.class).skipLimit(20).build();
    }

    @Bean
    // Configura el job para procesar Interés Mensual
    public Job interesMensualJob(JobRepository jobRepository, Step interesMensualStep) {
        return new JobBuilder("interesMensualJob", jobRepository).start(interesMensualStep).build();
    }
}