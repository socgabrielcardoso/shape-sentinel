package io.github.socgabrielcardoso.shapesentinel.core;

import java.util.Objects;

public record ShapeAssessment(ShapeType type, double confidence) {
    public ShapeAssessment {
        Objects.requireNonNull(type, "type");
        if (!Double.isFinite(confidence)) {
            throw new IllegalArgumentException("Confidence must be finite");
        }
        confidence = Math.max(0, Math.min(1, confidence));
    }
}
