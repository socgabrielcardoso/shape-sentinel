package io.github.socgabrielcardoso.shapesentinel.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapeClassifierHardeningTest {
    private final ShapeClassifier classifier = new ShapeClassifier();

    @Test
    void rejectsAreaLargerThanBoundingRegion() {
        assertEquals(ShapeType.UNKNOWN, classifier.classify(4, 800, 120, 20, 20, true).type());
    }

}
