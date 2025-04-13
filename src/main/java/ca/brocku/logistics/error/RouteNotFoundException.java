package ca.brocku.logistics.error;

import ca.brocku.logistics.model.Route;

public class RouteNotFoundException extends RouteException {

    public RouteNotFoundException(Route route) {
        super(route, "Could not find a route");
    }
}
