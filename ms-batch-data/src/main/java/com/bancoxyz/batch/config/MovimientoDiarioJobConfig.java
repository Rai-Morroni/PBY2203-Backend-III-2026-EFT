package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.MovimientoDiarioDTO;
import com.bancoxyz.batch.listener.RechazoProporcionListener;
import com.bancoxyz.batch.listener.RechazoSkipListener;
import com.bancoxyz.batch.model.MovimientosFinancierosDiarios;
import com.bancoxyz.batch.processor.MovimientoDiarioProcessor;
import com.bancoxyz.batch.service.RechazoService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MovimientoDiarioJobConfig {

    private static final String FILE_NAME = "movimientos_financieros_diarios.csv";

    @Bean
    public FlatFileItemReader<MovimientoDiarioDTO> movimientoDiarioReader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("cuentaId", "fecha", "tipo", "monto");
        BeanWrapperFieldSetMapper<MovimientoDiarioDTO> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(MovimientoDiarioDTO.class);
        DefaultLineMapper<MovimientoDiarioDTO> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);
        return new FlatFileItemReaderBuilder<MovimientoDiarioDTO>()
                .name("movimientoDiarioReader")
                .resource(new ClassPathResource("data/" + FILE_NAME))
                .linesToSkip(1)
                .lineMapper(new SourceLineMapper<>(lineMapper, FILE_NAME))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<MovimientosFinancierosDiarios> movimientoDiarioWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<MovimientosFinancierosDiarios>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO movimientos_diarios
                            (cuenta_id, fecha, tipo, monto, archivo_origen, linea_origen)
                        VALUES (:cuentaId, :fecha, :tipo, :monto, :archivoOrigen, :lineaOrigen)
                        ON DUPLICATE KEY UPDATE
                            cuenta_id = VALUES(cuenta_id), fecha = VALUES(fecha), tipo = VALUES(tipo),
                            monto = VALUES(monto)
                        """)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step movimientoDiarioStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<MovimientoDiarioDTO> movimientoDiarioReader,
                                     MovimientoDiarioProcessor processor,
                                     JdbcBatchItemWriter<MovimientosFinancierosDiarios> movimientoDiarioWriter,
                                     BatchProperties properties, RechazoService rechazoService) {
        return new StepBuilder("movimientoDiarioStep", jobRepository)
                .<MovimientoDiarioDTO, MovimientosFinancierosDiarios>chunk(100, transactionManager)
                .reader(movimientoDiarioReader)
                .processor(processor)
                .writer(movimientoDiarioWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(com.bancoxyz.batch.validation.RegistroInvalidoException.class)
                .skipLimit(properties.getSkipLimit())
                .listener(new RechazoSkipListener<>("movimientoDiarioJob", FILE_NAME, rechazoService))
                .listener(new RechazoProporcionListener(properties))
                .build();
    }

    @Bean
    public Job movimientoDiarioJob(JobRepository jobRepository, Step movimientoDiarioStep) {
        return new JobBuilder("movimientoDiarioJob", jobRepository).start(movimientoDiarioStep).build();
    }
}
