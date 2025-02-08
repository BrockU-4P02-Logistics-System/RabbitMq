package ca.brocku.logistics.model;

import java.util.List;

public class GeoJsonFeature {
    private String type;
    private Geometry geometry;
    private Properties properties;

    public GeoJsonFeature() {}
    public GeoJsonFeature(String type, Geometry geometry, Properties properties) {
        this.type = type;
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static class Geometry {
        private String type;
        private List<Double> coordinates;

        public Geometry() {}
        public Geometry(String type, List<Double> coordinates) {
            this.type = type;
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }
    }

    public static class Properties {
        private String address;
        private int order;

        public Properties() {}
        public Properties(String address, int order) {
            this.address = address;
            this.order = order;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }
}