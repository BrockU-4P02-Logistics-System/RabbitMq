package ca.brocku.logistics.algorithm;

import ca.brocku.logistics.model.Address;
import ca.brocku.logistics.model.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GeneticAlgorithm {

    private final Route route;

    public GeneticAlgorithm(Route route) {
        this.route = route;
    }

    public CompletableFuture<List<Address>> compute() {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

}
