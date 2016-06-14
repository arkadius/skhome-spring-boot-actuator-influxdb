package net.skhome.boot.actuate.metrics.influxdb;

import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.dto.Point;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.GaugeWriter;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A {@link GaugeWriter} for the InfluxDB time series database.
 *
 * @author Sascha Krueger
 */
@CommonsLog
public class InfluxDbGaugeWriter implements GaugeWriter {

    private final InfluxDB influxDB;
    private final String databaseName;
    private boolean isInitialized = false;

    @Getter
    private Map<String, String> tags = new HashMap<>();

    public InfluxDbGaugeWriter(final InfluxDB influxDB, final String databaseName) {
        Assert.notNull(influxDB, "The parameter influxDB may not be null.");
        Assert.notNull(databaseName, "The parameter databaseName may not be null.");

        this.influxDB = influxDB;
        this.databaseName = databaseName;
        this.initializeDatabase();
    }

    private void initializeDatabase() {
        if (!isInitialized) {
            try {
                this.influxDB.createDatabase(databaseName);
                this.isInitialized = true;
            } catch (Exception ex) {
                log.warn("Could not create InfluxDB database '" + databaseName + "'. Cause: " + ex.getMessage());
            }
        }
    }

    /**
     * Enable batching of single point writes to speed up writes significantly. If either actions or flushDurations is reached
     * first, a batched write is issued.
     *
     * @param actions the number of actions to collect
     * @param flushDuration the time to wait at most.
     * @param flushDurationTimeUnit the unit for the flushDuration
     */
    public void enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit) {
        this.influxDB.enableBatch(actions, flushDuration, flushDurationTimeUnit);
    }

    /**
     * Set the log level which is used for REST related actions.
     *
     * @param logLevel the log level to set.
     */
    public void setLogLevel(final LogLevel logLevel) {
        this.influxDB.setLogLevel(logLevel);
    }

	/**
     * Returns whether batch mode is enabled
     */
    public boolean isBatchEnabled() {
        return influxDB.isBatchEnabled();
    }

    @Override
    public void set(final Metric<?> value) {
        this.initializeDatabase();
        if (isInitialized) {
            final Point point = Point.measurement(value.getName())
                    .time(value.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
                    .addField("value", value.getValue())
                    .tag(tags)
                    .build();
            try {
                influxDB.write(databaseName, "default", point);
            } catch (Exception ex) {
                log.warn("Could not write metric '" + value.toString() + "' to InfluxDB. Cause: " + ex.getMessage());
            }
        }
    }
}
