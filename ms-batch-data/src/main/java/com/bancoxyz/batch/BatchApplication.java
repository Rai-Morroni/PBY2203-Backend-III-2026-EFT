package com.bancoxyz.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.bancoxyz.batch.config.BatchProperties;

@SpringBootApplication
@EnableConfigurationProperties(BatchProperties.class)
public class BatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

}
