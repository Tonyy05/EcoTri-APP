package iut.dam.ecotri.data;

import java.util.List;

public class BanResponse {
    public List<Feature> features;

    public static class Feature {
        public Properties properties;
        public Geometry geometry;
    }

    public static class Properties {
        public String label;
        public double score;
        public String city;
        public String postcode;
        public String type; // housenumber, street, municipality...
        public String id;
    }

    public static class Geometry {
        public double[] coordinates; // [lon, lat]
    }
}
