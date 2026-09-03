package io.github.socgabrielcardoso.shapesentinel.core;

public record DetectionSettings(double minimumArea, double edgeThreshold) {
    public static final double DEFAULT_MINIMUM_AREA = 1_200;
    public static final double DEFAULT_EDGE_THRESHOLD = 70;
    public static final double MINIMUM_AREA_LIMIT = 100;
    public static final double MINIMUM_EDGE_THRESHOLD = 10;
    public static final double MAXIMUM_EDGE_THRESHOLD = 250;

    public DetectionSettings {
        if (!Double.isFinite(minimumArea) || minimumArea < MINIMUM_AREA_LIMIT) {
            throw new IllegalArgumentException("Minimum area must be at least 100");
        }
        if (!Double.isFinite(edgeThreshold)
                || edgeThreshold < MINIMUM_EDGE_THRESHOLD
                || edgeThreshold > MAXIMUM_EDGE_THRESHOLD) {
            throw new IllegalArgumentException("Edge threshold must be between 10 and 250");
        }
    }

    public static DetectionSettings defaults() {
        return new DetectionSettings(DEFAULT_MINIMUM_AREA, DEFAULT_EDGE_THRESHOLD);
    }

    public double highThreshold() {
        return edgeThreshold * 2.4;
    }
}
