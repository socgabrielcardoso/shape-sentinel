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


    @Test
    void acceptsSmallBoundingAreaRoundingDiscrepancies() {
        assertEquals(ShapeType.SQUARE, classifier.classify(4, 10200, 400, 100, 100, true).type());
    }


    @Test
    void rejectsImpossibleConcaveTriangles() {
        assertEquals(ShapeType.UNKNOWN, classifier.classify(3, 120, 60, 20, 20, false).type());
    }


    @Test
    void treatsConcaveFiveSidedContoursAsGenericPolygons() {
        assertEquals(ShapeType.POLYGON, classifier.classify(5, 200, 70, 30, 30, false).type());
    }


    @Test
    void rejectsImpossibleCircularityAboveOne() {
        assertEquals(ShapeType.UNKNOWN, classifier.classify(9, 10000, 100, 200, 200, true).type());
    }


    @Test
    void doesNotConfuseLowCircularityWithCircularObjects() {
        double area = 0.5 * 100 * 100 / (4 * Math.PI);
        assertEquals(ShapeType.POLYGON, classifier.classify(9, area, 100, 50, 50, true).type());
    }


    @Test
    void keepsHighCircularityConfidenceInsideProbabilityRange() {
        double area = 0.999 * 100 * 100 / (4 * Math.PI);
        ShapeAssessment result = classifier.classify(12, area, 100, 50, 50, true);
        assertTrue(result.confidence() <= 1 && result.confidence() > 0.95);
    }

}
