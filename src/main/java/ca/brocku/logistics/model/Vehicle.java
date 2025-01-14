package ca.brocku.logistics.model;

import ca.brocku.logistics.api.VehicleType;
import ca.brocku.logistics.util.Metric;

/**
 * Vehicle Record
 * Contains all required configurations for vehicles
 *
 * @param vehicleType {@link VehicleType}
 * @param maximumSpeed {@link Metric}
 * @param weight {@link Metric}
 * @param height {@link Metric}
 * @param width {@link Metric}
 */
public record Vehicle(VehicleType vehicleType, Metric maximumSpeed, Metric weight, Metric height, Metric width) {}
