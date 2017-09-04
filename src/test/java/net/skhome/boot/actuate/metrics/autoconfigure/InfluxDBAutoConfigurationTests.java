package net.skhome.boot.actuate.metrics.autoconfigure;

import net.skhome.boot.actuate.metrics.influxdb.InfluxDbGaugeWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = InfluxDBAutoConfigurationTests.DemoApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
public class InfluxDBAutoConfigurationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private InfluxDbProperties properties;

    @Test
    public void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    public void exportsMetricsWriter() {
        assertThat(context.containsBean("influxDbWriter")).isTrue();
    }

    @Test
    public void configuresTags() {
        final InfluxDbGaugeWriter writer = context.getBean(InfluxDbGaugeWriter.class);
        assertThat(writer.getTags()).containsAllEntriesOf(properties.getTags());
    }

    @Test
    public void enablesBatch() {
        final InfluxDbGaugeWriter writer = context.getBean(InfluxDbGaugeWriter.class);
        assertThat(writer.isBatchEnabled()).isTrue();
    }

    @SpringBootApplication
    public static class DemoApplication {

        public static void main(final String[] args) {
            SpringApplication.run(DemoApplication.class, args);
        }

    }
}
