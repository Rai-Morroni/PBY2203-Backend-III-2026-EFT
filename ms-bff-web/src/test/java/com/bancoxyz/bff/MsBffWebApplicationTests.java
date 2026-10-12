package com.bancoxyz.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.config.import=optional:configserver:http://localhost:8888",
		"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9000/oauth2/jwks"
})
class MsBffWebApplicationTests {

	@Test
	void contextLoads() {
	}

}
