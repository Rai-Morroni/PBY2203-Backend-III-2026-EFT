package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.InteresMensualDTO;
import com.bancoxyz.batch.listener.RechazoProporcionListener;
import com.bancoxyz.batch.listener.RechazoSkipListener;
import com.bancoxyz.batch.model.InteresMensual;
import com.bancoxyz.batch.processor.InteresMensualProcessor;
import com.bancoxyz.batch.service.RechazoService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.YearMonth;

@Configuration
public class InteresMensualJobConfig {

    private static final String FILE_NAME = "intereses_trimestrales.csv";

    @Bean
    public FlatFileItemReader<InteresMensualDTO> interesMensualReader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("cuentaId", "nombre", "saldo", "edad", "tipo");
        BeanWrapperFieldSetMapper<InteresMensualDTO> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(InteresMensualDTO.class);
        DefaultLineMapper<InteresMensualDTO> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);
        return new FlatFileItemReaderBuilder<InteresMensualDTO>()
                .name("interesMensualReader")
                .resource(new ClassPathResource("data/" + FILE_NAME))
                .linesToSkip(1)
                .lineMapper(new SourceLineMapper<>(lineMapper, FILE_NAME))
                .build();
    }

    @Bean
    @StepScope
    public InteresMensualProcessor interesMensualProcessor(BatchProperties properties,
            @Value("#{jobParameters['periodo']}") String periodo) {
        return new InteresMensualProcessor(properties, YearMonth.parse(periodo));
    }

    @Bean
    public JdbcBatchItemWriter<InteresMensual> interesMensualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<InteresMensual>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO intereses_mensuales
                            (cuenta_id, nombre, saldo, edad, tipo, saldo_base, tasa_anual, tasa_mensual,
                             interes_mensual, saldo_final, periodo, archivo_origen, linea_origen)
                        VALUES (:cuentaId, :nombre, :saldo, :edad, :tipo, :saldoBase, :tasaAnual, :tasaMensual,
                                :interesMensual, :saldoFinal, :periodo, :archivoOrigen, :lineaOrigen)
                        ON DUPLICATE KEY UPDATE
                            cuenta_id = VALUES(cuenta_id), nombre = VALUES(nombre), saldo = VALUES(saldo),
                            edad = VALUES(edad), tipo = VALUES(tipo), saldo_base = VALUES(saldo_base),
                            tasa_anual = VALUES(tasa_anual), tasa_mensual = VALUES(tasa_mensual),
                            interes_mensual = VALUES(interes_mensual), saldo_final = VALUES(saldo_final)
                        """)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step interesMensualStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<InteresMensualDTO> interesMensualReader,
                                   InteresMensualProcessor interesMensualProcessor,
                                   JdbcBatchItemWriter<InteresMensual> interesMensualWriter,
                                   BatchProperties properties, RechazoService rechazoService) {
        return new StepBuilder("interesMensualStep", jobRepository)
                .<InteresMensualDTO, InteresMensual>chunk(100, transactionManager)
                .reader(interesMensualReader)
                .processor(interesMensualProcessor)
                .writer(interesMensualWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(com.bancoxyz.batch.validation.RegistroInvalidoException.class)
                .skipLimit(properties.getSkipLimit())
                .listener(new RechazoSkipListener<>("interesMensualJob", FILE_NAME, rechazoService))
                .listener(new RechazoProporcionListener(properties))
                .build();
    }

    @Bean
    public Job interesMensualJob(JobRepository jobRepository, Step interesMensualStep) {
        return new JobBuilder("interesMensualJob", jobRepository).start(interesMensualStep).build();
    }
}
