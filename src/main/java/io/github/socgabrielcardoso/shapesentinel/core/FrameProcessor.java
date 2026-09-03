package io.github.socgabrielcardoso.shapesentinel.core;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FrameProcessor implements AutoCloseable {
    private static final Scalar TEXT_COLOR = new Scalar(247, 250, 252);
    private static final Scalar PANEL_COLOR = new Scalar(24, 28, 36);

    private final ShapeClassifier classifier = new ShapeClassifier();
    private final Mat gray = new Mat();
    private final Mat blurred = new Mat();
    private final Mat edges = new Mat();
    private final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

    public int process(Mat frame, DetectionSettings settings, double fps) {
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);
        Imgproc.Canny(blurred, edges, settings.edgeThreshold(), settings.highThreshold());
        Imgproc.morphologyEx(edges, edges, Imgproc.MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat contourInput = edges.clone();
        try {
            Imgproc.findContours(
                    contourInput,
                    contours,
                    hierarchy,
                    Imgproc.RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE
            );
            int detected = countDetections(frame, settings, contours);
            drawMetrics(frame, detected, fps);
            return detected;
        } finally {
            contourInput.release();
            hierarchy.release();
            contours.forEach(MatOfPoint::release);
        }
    }

    private int countDetections(Mat frame, DetectionSettings settings, List<MatOfPoint> contours) {
        int detected = 0;
        double frameArea = frame.width() * (double) frame.height();

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < settings.minimumArea() || area > frameArea * 0.95) {
                continue;
            }

            MatOfPoint2f curve = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approximation = new MatOfPoint2f();
            try {
                double perimeter = Imgproc.arcLength(curve, true);
                Imgproc.approxPolyDP(curve, approximation, perimeter * 0.025, true);

                MatOfPoint polygon = new MatOfPoint(approximation.toArray());
                try {
                    RotatedRect box = Imgproc.minAreaRect(approximation);
                    boolean convex = Imgproc.isContourConvex(polygon);
                    ShapeAssessment assessment = classifier.classify(
                            (int) approximation.total(),
                            area,
                            perimeter,
                            box.size.width,
                            box.size.height,
                            convex
                    );

                    if (assessment.type() == ShapeType.UNKNOWN) {
                        continue;
                    }

                    Rect bounds = Imgproc.boundingRect(polygon);
                    drawDetection(frame, contour, bounds, assessment, area);
                    detected++;
                } finally {
                    polygon.release();
                }
            } finally {
                curve.release();
                approximation.release();
            }
        }

        return detected;
    }

    private void drawDetection(
            Mat frame,
            MatOfPoint contour,
            Rect bounds,
            ShapeAssessment assessment,
            double area
    ) {
        Scalar color = colorFor(assessment.type());
        Imgproc.drawContours(frame, List.of(contour), -1, color, 3, Imgproc.LINE_AA);

        String text = String.format(
                Locale.ROOT,
                "%s  %.0f px  %.0f%%",
                assessment.type().displayName(),
                area,
                assessment.confidence() * 100
        );

        int[] baseline = new int[1];
        Size textSize = Imgproc.getTextSize(text, Imgproc.FONT_HERSHEY_SIMPLEX, 0.55, 1, baseline);
        int labelWidth = (int) Math.ceil(textSize.width);
        int labelHeight = (int) Math.ceil(textSize.height);
        int maxX = Math.max(4, frame.width() - labelWidth - 6);
        int x = clamp(bounds.x, 4, maxX);
        int preferredY = bounds.y > labelHeight + 16 ? bounds.y - 8 : bounds.y + labelHeight + 12;
        int maxY = Math.max(labelHeight + 7, frame.height() - baseline[0] - 5);
        int textY = clamp(preferredY, labelHeight + 7, maxY);
        Point panelStart = new Point(x - 4, textY - textSize.height - 7);
        Point panelEnd = new Point(
                Math.min(frame.width() - 1, x + textSize.width + 5),
                Math.min(frame.height() - 1, textY + baseline[0] + 4)
        );

        Imgproc.rectangle(frame, panelStart, panelEnd, PANEL_COLOR, Imgproc.FILLED);
        Imgproc.putText(
                frame,
                text,
                new Point(x, textY),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.55,
                TEXT_COLOR,
                1,
                Imgproc.LINE_AA
        );
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    private void drawMetrics(Mat frame, int detected, double fps) {
        String metrics = String.format(Locale.ROOT, "SHAPES %02d   FPS %04.1f", detected, fps);
        Imgproc.rectangle(frame, new Point(16, 16), new Point(235, 52), PANEL_COLOR, Imgproc.FILLED);
        Imgproc.putText(
                frame,
                metrics,
                new Point(28, 41),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.58,
                new Scalar(78, 224, 181),
                1,
                Imgproc.LINE_AA
        );
    }

    private Scalar colorFor(ShapeType type) {
        return switch (type) {
            case TRIANGLE -> new Scalar(80, 214, 255);
            case SQUARE -> new Scalar(93, 234, 123);
            case RECTANGLE -> new Scalar(255, 181, 92);
            case PENTAGON -> new Scalar(226, 132, 255);
            case HEXAGON -> new Scalar(255, 111, 145);
            case CIRCLE -> new Scalar(84, 224, 184);
            case POLYGON -> new Scalar(218, 218, 218);
            case UNKNOWN -> new Scalar(128, 128, 128);
        };
    }

    @Override
    public void close() {
        gray.release();
        blurred.release();
        edges.release();
        kernel.release();
    }
}
