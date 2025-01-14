package ca.brocku.logistics.error;

import ca.brocku.logistics.model.Route;

public abstract class RouteException extends RuntimeException {

    private final Route route;

    public RouteException(Route route) {
        this(route, null);
    }

    public RouteException(Route route, String message) {
        super(message);
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }
}
