package com.bancoxyz.ms_bff_cajero;

import com.bancoxyz.msbffcajero.MsBffCajeroApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = MsBffCajeroApplication.class,
		properties = {
				"spring.config.import=optional:configserver:http://localhost:8888",
				"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9000/oauth2/jwks"
		}
)
class MsBffCajeroApplicationTests {

	@Test
	void contextLoads() {
	}

}
