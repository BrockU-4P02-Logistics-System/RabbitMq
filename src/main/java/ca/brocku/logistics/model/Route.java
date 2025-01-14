package ca.brocku.logistics.model;

import ca.brocku.logistics.api.Filters;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Route Record
 * Contains all information for vehicle(s) and required route(s)
 * Including filter options for specific routes
 *
 * @param vehicles {@link Vehicle}
 * @param addresses {@link Address}
 * @param departureTime
 * @param filters {@link Filters}
 */
public record Route(Collection<Vehicle> vehicles, Collection<Address> addresses, Date departureTime, Set<Filters> filters) {}
