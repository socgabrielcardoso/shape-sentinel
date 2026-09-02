package io.github.socgabrielcardoso.shapesentinel.core;

public record DetectionSettings(double minimumArea, double edgeThreshold) {
    public DetectionSettings {
        if (minimumArea < 100) {
            throw new IllegalArgumentException("Minimum area must be at least 100");
        }
        if (edgeThreshold < 10 || edgeThreshold > 250) {
            throw new IllegalArgumentException("Edge threshold must be between 10 and 250");
        }
    }

    public double highThreshold() {
        return edgeThreshold * 2.4;
    }
}
