package com.bancoxyz.msauthserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.config.import=optional:configserver:http://localhost:8888")
class MsAuthServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
