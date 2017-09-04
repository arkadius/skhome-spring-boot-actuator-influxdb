package net.skhome.boot.actuate.metrics.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.influxdb.InfluxDB.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.metrics.export.influxdb")
public class InfluxDbProperties {

    /**
     * The URL for the InfluxDB REST interface.
     * (e.g. http://localhost:8086)
     */
    private URL url;

    /**
     * The username needed to connect to InfluxDB.
     */
    private String username;

    /**
     * The password needed to connect to InfluxDB.
     */
    private String password;

    /**
     * The InfluxDB database name to write metrics to.
     * If this database does not already exist it will be created.
     */
    private String database = "aTimeSeries";

    private String retentionPolicy = "autogen";

    private BatchMode batch;

    /**
     * Controls the amount of logging for the REST layer.
     * Possible values are none (default), basic, headers, full
     */
    private LogLevel logLevel = LogLevel.NONE;

    private Map<String, String> tags = new HashMap<>();

    @Getter
    @Setter
    public static class BatchMode {

        /**
         * Whether batch mode should be enabled.
         * This might increase performance and lower the number of calls to InfluxDB.
         */
        private boolean enabled = true;

        /**
         * The number of metrics to be accumulated before they are written to InfluxDB.
         */
        private int actions = 100;

        /**
         * The number of milliseconds before metrics are written to InfluxDB.
         */
        private int flushDuration = 1000;

    }
}
