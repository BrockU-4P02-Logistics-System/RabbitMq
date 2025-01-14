package ca.brocku.logistics.util;

import ca.brocku.logistics.api.MetricType;

/**
 * Metric Record
 *
 * @param number
 * @param metricType {@link MetricType}
 */
public record Metric(Number number, MetricType metricType) {

}
