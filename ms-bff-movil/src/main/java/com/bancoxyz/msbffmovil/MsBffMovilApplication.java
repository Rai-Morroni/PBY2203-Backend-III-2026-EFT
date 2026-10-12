package com.bancoxyz.msbffmovil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // Habilita la inyección de clientes Feign
public class MsBffMovilApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsBffMovilApplication.class, args);
	}

}
