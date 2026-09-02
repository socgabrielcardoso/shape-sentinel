package io.github.socgabrielcardoso.shapesentinel.core;

public final class ShapeClassifier {
    public ShapeAssessment classify(
            int vertices,
            double area,
            double perimeter,
            double width,
            double height,
            boolean convex
    ) {
        if (vertices < 3 || area <= 0 || perimeter <= 0 || width <= 0 || height <= 0) {
            return new ShapeAssessment(ShapeType.UNKNOWN, 0);
        }

        if (!convex) {
            return new ShapeAssessment(ShapeType.POLYGON, 0.68);
        }

        return switch (vertices) {
            case 3 -> new ShapeAssessment(ShapeType.TRIANGLE, 0.94);
            case 4 -> classifyQuadrilateral(width, height);
            case 5 -> new ShapeAssessment(ShapeType.PENTAGON, 0.92);
            case 6 -> new ShapeAssessment(ShapeType.HEXAGON, 0.91);
            default -> classifyRoundedShape(area, perimeter);
        };
    }

    private ShapeAssessment classifyQuadrilateral(double width, double height) {
        double ratio = Math.min(width, height) / Math.max(width, height);
        if (ratio >= 0.86) {
            return new ShapeAssessment(ShapeType.SQUARE, 0.82 + ratio * 0.16);
        }
        return new ShapeAssessment(ShapeType.RECTANGLE, 0.88 + ratio * 0.08);
    }

    private ShapeAssessment classifyRoundedShape(double area, double perimeter) {
        double circularity = 4 * Math.PI * area / (perimeter * perimeter);
        if (circularity >= 0.76) {
            double confidence = 0.78 + Math.min(1, circularity) * 0.2;
            return new ShapeAssessment(ShapeType.CIRCLE, confidence);
        }
        return new ShapeAssessment(ShapeType.POLYGON, 0.72);
    }
}
