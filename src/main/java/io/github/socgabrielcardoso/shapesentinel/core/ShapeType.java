package io.github.socgabrielcardoso.shapesentinel.core;

public enum ShapeType {
    TRIANGLE("Triangle"),
    SQUARE("Square"),
    RECTANGLE("Rectangle"),
    PENTAGON("Pentagon"),
    HEXAGON("Hexagon"),
    CIRCLE("Circle"),
    POLYGON("Polygon"),
    UNKNOWN("Unknown");

    private final String displayName;

    ShapeType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
