package net.skhome.boot.actuate.metrics.influxdb;

import org.influxdb.dto.Point;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.util.Assert;

import java.util.Arrays;

class ActuatorPointExtractor {

    private static final int METRIC_TYPE_POSITION = 0; // counter, gauge, ...
    private static final int METRIC_NAME_POSITION = METRIC_TYPE_POSITION + 1; // status, response, ...
    private static final int METRIC_HTTP_CODE_POSITION = METRIC_NAME_POSITION + 1;
    private static final int METRIC_STATUS_HTTP_SERVICE_POSITION = METRIC_HTTP_CODE_POSITION + 1;
    private static final int METRIC_RESPONSE_HTTP_SERVICE_POSITION = METRIC_NAME_POSITION + 1;

    static Point.Builder extract(final Metric<?> actuateMetric) {
        String metricName = actuateMetric.getName();
        String[] parts = metricName.split("\\.");
        if (parts.length < METRIC_NAME_POSITION + 1) {
            return Point.measurement(metricName);
        }
        String[] endpointParts;
        String endpoint;
        switch (parts[METRIC_NAME_POSITION]) {
            case "status":
                Assert.isTrue(parts.length > METRIC_STATUS_HTTP_SERVICE_POSITION, "Accurate status metric has missing endpoint id part");
                endpointParts = Arrays.copyOfRange(parts, METRIC_STATUS_HTTP_SERVICE_POSITION, parts.length);
                endpoint = String.join(".", endpointParts);
                return Point.measurement(parts[METRIC_TYPE_POSITION] + "." + parts[METRIC_NAME_POSITION])
                        .tag("endpoint", endpoint)
                        .tag("code", parts[METRIC_HTTP_CODE_POSITION]);
            case "response":
                Assert.isTrue(parts.length > METRIC_RESPONSE_HTTP_SERVICE_POSITION, "Accurate response metric has missing endpoint id part");
                endpointParts = Arrays.copyOfRange(parts, METRIC_RESPONSE_HTTP_SERVICE_POSITION, parts.length);
                endpoint = String.join(".", endpointParts);
                return Point.measurement(parts[METRIC_TYPE_POSITION] + "." + parts[METRIC_NAME_POSITION])
                        .tag("endpoint", endpoint);
            default:
                return Point.measurement(metricName);

        }
    }


}