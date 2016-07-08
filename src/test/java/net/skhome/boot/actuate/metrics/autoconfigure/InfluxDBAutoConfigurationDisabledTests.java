package net.skhome.boot.actuate.metrics.autoconfigure;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InfluxDBAutoConfigurationDisabledTests.DemoApplication.class)
@TestPropertySource(locations = "classpath:application-disabled.properties")
public class InfluxDBAutoConfigurationDisabledTests {

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    public void exportsMetricsWriter() {
        assertThat(context.containsBean("influxDbWriter")).isFalse();
    }

    @SpringBootApplication
    public static class DemoApplication {

        public static void main(final String[] args) {
            SpringApplication.run(DemoApplication.class, args);
        }

    }
}
