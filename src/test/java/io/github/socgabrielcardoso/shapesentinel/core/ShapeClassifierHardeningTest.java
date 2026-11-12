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

}
