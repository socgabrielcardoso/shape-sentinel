package io.github.socgabrielcardoso.shapesentinel.core;

public record ShapeAssessment(ShapeType type, double confidence) {
    public ShapeAssessment {
        confidence = Math.max(0, Math.min(1, confidence));
    }
}
