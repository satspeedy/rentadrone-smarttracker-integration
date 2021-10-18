package com.hha.dronesim;

import com.hha.dronesim.config.DaprConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@DirtiesContext
@ContextConfiguration(classes = DaprConfiguration.class)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:49092", "port=49092"})
class DroneSimApplicationTests {

    @Test
    @SuppressWarnings("java:S2699")
    void contextLoads() {
    }

}
