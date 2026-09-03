package io.github.socgabrielcardoso.shapesentinel.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShapeAssessmentTest {
    @Test
    void clampsConfidenceToValidRange() {
        assertEquals(1, new ShapeAssessment(ShapeType.CIRCLE, 2).confidence());
        assertEquals(0, new ShapeAssessment(ShapeType.UNKNOWN, -1).confidence());
    }

    @Test
    void requiresShapeType() {
        assertThrows(NullPointerException.class, () -> new ShapeAssessment(null, 0.5));
    }
}
