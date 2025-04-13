package ca.brocku.logistics.model;

import java.util.List;

public class MessageRequest {
    private List<GeoJsonFeature> features;
    private int numberDrivers = 1;
    private boolean returnToStart = false;

    // Getters and setters
    public List<GeoJsonFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<GeoJsonFeature> features) {
        this.features = features;
    }

    public int getNumberDrivers() {
        return numberDrivers;
    }

    public void setNumberDrivers(int numberDrivers) {
        this.numberDrivers = numberDrivers;
    }

    public boolean isReturnToStart() {
        return returnToStart;
    }

    public void setReturnToStart(boolean returnToStart) {
        this.returnToStart = returnToStart;
    }
}