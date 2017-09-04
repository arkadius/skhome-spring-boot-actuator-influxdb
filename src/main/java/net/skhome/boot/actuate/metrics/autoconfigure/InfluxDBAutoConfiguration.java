package net.skhome.boot.actuate.metrics.autoconfigure;

import net.skhome.boot.actuate.metrics.influxdb.InfluxDbGaugeWriter;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


/**
 * Auto configuration for exporting metrics to the InfluxDB time series database.
 *
 * @author Sascha Krüger
 */
@Configuration
@ConditionalOnClass(InfluxDB.class)
@ConditionalOnProperty(prefix = "spring.metrics.export.influxdb", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(InfluxDbProperties.class)
public class InfluxDBAutoConfiguration {

    @Autowired
    private InfluxDbProperties properties;

    @Bean
    @ExportMetricWriter
    @ConditionalOnMissingBean(InfluxDbGaugeWriter.class)
    public InfluxDbGaugeWriter influxDbWriter() {
        final InfluxDbGaugeWriter writer = new InfluxDbGaugeWriter(influxDB(), properties.getDatabase(), properties.getRetentionPolicy());
        final InfluxDbProperties.BatchMode batchMode = properties.getBatch();
        if ((batchMode != null) && (batchMode.isEnabled())) {
            writer.enableBatch(batchMode.getActions(), batchMode.getFlushDuration(), TimeUnit.MILLISECONDS);
        }
        writer.setLogLevel(properties.getLogLevel());
        writer.getTags().putAll(properties.getTags());
        return writer;
    }

    private InfluxDB influxDB() {
        return InfluxDBFactory.connect(properties.getUrl().toString(),
                                       properties.getUsername(),
                                       properties.getPassword());
    }

}
