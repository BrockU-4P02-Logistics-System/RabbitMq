package ca.brocku.logistics.error;

public class RouteParseException extends RuntimeException {

    public RouteParseException() {
        super("Could not parse the route from JSON");
    }
}
