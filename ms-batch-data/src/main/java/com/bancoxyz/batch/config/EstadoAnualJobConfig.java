package com.bancoxyz.batch.config;

import com.bancoxyz.batch.dto.EstadoAnualDTO;
import com.bancoxyz.batch.listener.RechazoProporcionListener;
import com.bancoxyz.batch.listener.RechazoSkipListener;
import com.bancoxyz.batch.model.EstadosFinancierosAnuales;
import com.bancoxyz.batch.processor.EstadoAnualProcessor;
import com.bancoxyz.batch.service.RechazoService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class EstadoAnualJobConfig {

    private static final String FILE_NAME = "estados_financieros_anuales.csv";

    @Bean
    public FlatFileItemReader<EstadoAnualDTO> estadoAnualReader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("cuentaId", "fecha", "transaccion", "monto", "descripcion");
        BeanWrapperFieldSetMapper<EstadoAnualDTO> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(EstadoAnualDTO.class);
        DefaultLineMapper<EstadoAnualDTO> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);
        return new FlatFileItemReaderBuilder<EstadoAnualDTO>()
                .name("estadoAnualReader")
                .resource(new ClassPathResource("data/" + FILE_NAME))
                .linesToSkip(1)
                .lineMapper(new SourceLineMapper<>(lineMapper, FILE_NAME))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<EstadosFinancierosAnuales> estadoAnualWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EstadosFinancierosAnuales>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO estados_anuales
                            (cuenta_id, fecha, transaccion, monto, descripcion, archivo_origen, linea_origen)
                        VALUES (:cuentaId, :fecha, :transaccion, :monto, :descripcion, :archivoOrigen, :lineaOrigen)
                        ON DUPLICATE KEY UPDATE
                            cuenta_id = VALUES(cuenta_id), fecha = VALUES(fecha), transaccion = VALUES(transaccion),
                            monto = VALUES(monto), descripcion = VALUES(descripcion)
                        """)
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step estadoAnualStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                FlatFileItemReader<EstadoAnualDTO> estadoAnualReader,
                                EstadoAnualProcessor processor,
                                JdbcBatchItemWriter<EstadosFinancierosAnuales> estadoAnualWriter,
                                BatchProperties properties, RechazoService rechazoService) {
        return new StepBuilder("estadoAnualStep", jobRepository)
                .<EstadoAnualDTO, EstadosFinancierosAnuales>chunk(100, transactionManager)
                .reader(estadoAnualReader)
                .processor(processor)
                .writer(estadoAnualWriter)
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(com.bancoxyz.batch.validation.RegistroInvalidoException.class)
                .skipLimit(properties.getSkipLimit())
                .listener(new RechazoSkipListener<>("estadoAnualJob", FILE_NAME, rechazoService))
                .listener(new RechazoProporcionListener(properties))
                .build();
    }

    @Bean
    public Job estadoAnualJob(JobRepository jobRepository, Step estadoAnualStep) {
        return new JobBuilder("estadoAnualJob", jobRepository).start(estadoAnualStep).build();
    }
}
