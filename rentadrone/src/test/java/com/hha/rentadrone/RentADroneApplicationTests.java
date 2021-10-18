package com.hha.rentadrone;

import com.hha.rentadrone.config.DaprConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@DirtiesContext
@ContextConfiguration(classes = DaprConfiguration.class)
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:49092", "port=49092" })
class RentADroneApplicationTests {

	@Test
	@SuppressWarnings("java:S2699")
	void contextLoads() {
	}

}
