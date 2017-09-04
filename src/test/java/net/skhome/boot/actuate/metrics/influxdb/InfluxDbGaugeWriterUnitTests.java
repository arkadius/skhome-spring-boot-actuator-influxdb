package net.skhome.boot.actuate.metrics.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InfluxDbGaugeWriterUnitTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private InfluxDB influxDB;

    @Captor
    private ArgumentCaptor<Point> pointCaptor;

    private InfluxDbGaugeWriter writer;

    @Before
    public void prepareWriter() {
        writer = new InfluxDbGaugeWriter(influxDB, "database", "default");
    }

    @Test
    public void rejectsMissingInfluxDB() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The parameter influxDB may not be null.");
        new InfluxDbGaugeWriter(null, "database", "default");
    }

    @Test
    public void rejectsMissingDatabaseName() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("The parameter databaseName may not be null.");
        new InfluxDbGaugeWriter(influxDB, null, "default");
    }

    @Test
    public void createsDatabase() {
        final String databaseName = "customDatabase";
        new InfluxDbGaugeWriter(influxDB, databaseName, "default");
        verify(influxDB).createDatabase(databaseName);
    }

    @Test
    public void enablesBatchedExport() {
        writer.enableBatch(2000, 500, TimeUnit.MILLISECONDS);
        verify(influxDB).enableBatch(2000, 500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void reportsBatchEnabledState() {
        given(influxDB.isBatchEnabled()).willReturn(true);
        assertThat(writer.isBatchEnabled()).isTrue();
    }

    @Test
    public void configuresLogLevel() {
        final LogLevel logLevel = LogLevel.BASIC;
        writer.setLogLevel(logLevel);
        verify(influxDB).setLogLevel(logLevel);
    }

    @Test
    public void writesPointUsingMetricNameAsMeasurementName() {
        // given
        final Metric<Integer> metric = new Metric<>("counter.test", 5);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasMeasurement("counter.test");
    }

    @Test
    public void writesIntegerPointValue() {
        // given
        final Metric<Integer> metric = new Metric<>("counter.test", 5);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasValue("5i");
    }

    @Test
    public void writesDoublePointValue() {
        // given
        final Metric<Double> metric = new Metric<>("gauge.test", 5.25);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasValue("5.25");
    }

    @Test
    public void writesTimestampFromMetric() {
        // given
        final Date timestamp = new Date();
        final Metric<Integer> metric = new Metric<>("counter.test", 5, timestamp);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasTimestamp(timestamp.getTime());
    }

    @Test
    public void writesTags() {
        // given
        writer.getTags().put("hostname", "localhost");
        writer.getTags().put("application.properties", "test-service");
        final Metric<Integer> metric = new Metric<>("counter.test", 5);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasTagWithValue("hostname", "localhost");
        PointAssert.assertThat(pointCaptor.getValue()).hasTagWithValue("application.properties", "test-service");
    }

    @Test
    public void writeStatusWithServiceAndCodeIdInTag() {
        // given
        final Metric<Integer> metric = new Metric<>("counter.status.200.Examples", 5);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasTagWithValue("service", "Examples");
        PointAssert.assertThat(pointCaptor.getValue()).hasTagWithValue("code", "200");
        PointAssert.assertThat(pointCaptor.getValue()).hasMeasurement("counter.status");
    }

    @Test
    public void writeResponseWithServiceIdInTag() {
        // given
        final Metric<Integer> metric = new Metric<>("gauge.response.Examples", 5);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasTagWithValue("service", "Examples");
        PointAssert.assertThat(pointCaptor.getValue()).hasMeasurement("gauge.response");
    }

    @Test
    public void writeResponseWithServiceIdWithMultiplePartsInTag() {
        // given
        final Metric<Integer> metric = new Metric<>("gauge.response.Foo.Bar", 5);
        // when
        writer.set(metric);
        // then
        verify(influxDB).write(anyString(), anyString(), pointCaptor.capture());
        PointAssert.assertThat(pointCaptor.getValue()).hasTagWithValue("service", "Foo.Bar");
        PointAssert.assertThat(pointCaptor.getValue()).hasMeasurement("gauge.response");
    }
}
