package net.skhome.boot.actuate.metrics.influxdb;

import org.assertj.core.api.AbstractAssert;
import org.influxdb.dto.Point;

public class PointAssert extends AbstractAssert<PointAssert, Point> {

    PointAssert(final Point actual) {
        super(actual, PointAssert.class);
    }

    public static PointAssert assertThat(final Point actual) {
        return new PointAssert(actual);
    }

    public PointAssert hasMeasurement(final String measurement) {
        isNotNull();
        if (!actual.lineProtocol().startsWith(measurement)) {
            failWithMessage(
                    "Expected points measurement to be <%s> but could not find it in <%s>",
                    measurement,
                    actual.lineProtocol());
        }
        return this;
    }

    public PointAssert hasValue(final String value) {
        isNotNull();
        if (!actual.lineProtocol().contains("value=" + value)) {
            failWithMessage("Expected points value to be <%s> but could not find it in <%s>", value, actual.lineProtocol());
        }
        return this;
    }

    public PointAssert hasTimestamp(final long timestamp) {
        isNotNull();
        if (!actual.lineProtocol().endsWith(String.valueOf(timestamp * 1000000))) {
            failWithMessage(
                    "Expected points timestamp to be <%d> but could not find it in <%s>",
                    timestamp,
                    actual.lineProtocol());
        }
        return this;
    }

    public PointAssert hasTagWithValue(final String name, final String value) {
        isNotNull();
        if (!actual.lineProtocol().contains(name + "=" + value)) {
            failWithMessage(
                    "Expected point to contain tag <%s> with value <%s> but could not find it in <%s>",
                    name,
                    value,
                    actual.lineProtocol());
        }
        return this;
    }
}
