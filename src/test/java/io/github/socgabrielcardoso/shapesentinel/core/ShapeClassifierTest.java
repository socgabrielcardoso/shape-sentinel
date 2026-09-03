package io.github.socgabrielcardoso.shapesentinel.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapeClassifierTest {
    private final ShapeClassifier classifier = new ShapeClassifier();

    @Test
    void detectsTriangle() {
        ShapeAssessment result = classifier.classify(3, 4_200, 300, 90, 85, true);

        assertEquals(ShapeType.TRIANGLE, result.type());
        assertTrue(result.confidence() > 0.9);
    }

    @Test
    void separatesSquareFromRectangle() {
        ShapeAssessment square = classifier.classify(4, 4_000, 260, 64, 62, true);
        ShapeAssessment rectangle = classifier.classify(4, 6_000, 360, 120, 50, true);

        assertEquals(ShapeType.SQUARE, square.type());
        assertEquals(ShapeType.RECTANGLE, rectangle.type());
    }

    @Test
    void detectsPentagonAndHexagon() {
        ShapeAssessment pentagon = classifier.classify(5, 5_000, 285, 80, 80, true);
        ShapeAssessment hexagon = classifier.classify(6, 5_800, 295, 86, 84, true);

        assertEquals(ShapeType.PENTAGON, pentagon.type());
        assertEquals(ShapeType.HEXAGON, hexagon.type());
    }

    @Test
    void detectsCircleByCircularity() {
        double radius = 40;
        double area = Math.PI * radius * radius;
        double perimeter = 2 * Math.PI * radius;
        ShapeAssessment result = classifier.classify(12, area, perimeter, 80, 80, true);

        assertEquals(ShapeType.CIRCLE, result.type());
        assertTrue(result.confidence() > 0.95);
    }

    @Test
    void rejectsInvalidGeometry() {
        ShapeAssessment result = classifier.classify(2, 0, 0, 0, 0, false);

        assertEquals(ShapeType.UNKNOWN, result.type());
        assertEquals(0, result.confidence());
    }

    @Test
    void rejectsNonFiniteGeometry() {
        ShapeAssessment result = classifier.classify(4, Double.NaN, 200, 50, 50, true);

        assertEquals(ShapeType.UNKNOWN, result.type());
        assertEquals(0, result.confidence());
    }

    @Test
    void respectsSquareRatioBoundary() {
        ShapeAssessment square = classifier.classify(4, 8_600, 372, 100, 86, true);
        ShapeAssessment rectangle = classifier.classify(4, 8_500, 370, 100, 85, true);

        assertEquals(ShapeType.SQUARE, square.type());
        assertEquals(ShapeType.RECTANGLE, rectangle.type());
    }

    @Test
    void respectsCircleCircularityBoundary() {
        double perimeter = 100;
        double circleArea = 0.76 * perimeter * perimeter / (4 * Math.PI);
        double polygonArea = 0.75 * perimeter * perimeter / (4 * Math.PI);

        ShapeAssessment circle = classifier.classify(10, circleArea, perimeter, 30, 30, true);
        ShapeAssessment polygon = classifier.classify(10, polygonArea, perimeter, 30, 30, true);

        assertEquals(ShapeType.CIRCLE, circle.type());
        assertEquals(ShapeType.POLYGON, polygon.type());
    }
}
