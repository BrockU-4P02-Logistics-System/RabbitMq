package ca.brocku.logistics.model;

import java.util.List;

public class Route {
    private List<GeoJsonFeature> features;

    public Route(List<GeoJsonFeature> features) {
        this.features = features;
    }

    public List<GeoJsonFeature> getFeatures() {
        return features;
    }
}