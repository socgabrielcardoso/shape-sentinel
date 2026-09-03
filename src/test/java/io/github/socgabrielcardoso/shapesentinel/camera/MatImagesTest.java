package io.github.socgabrielcardoso.shapesentinel.camera;

import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatImagesTest {
    @BeforeAll
    static void loadOpenCv() {
        OpenCV.loadLocally();
    }

    @Test
    void convertsThreeChannelImage() {
        Mat source = new Mat(2, 3, CvType.CV_8UC3);
        try {
            source.put(0, 0, new byte[18]);

            BufferedImage image = MatImages.toBufferedImage(source);

            assertEquals(3, image.getWidth());
            assertEquals(2, image.getHeight());
            assertEquals(BufferedImage.TYPE_3BYTE_BGR, image.getType());
        } finally {
            source.release();
        }
    }

    @Test
    void rejectsUnsupportedChannelCount() {
        Mat source = new Mat(2, 2, CvType.CV_8UC4);
        try {
            assertThrows(IllegalArgumentException.class, () -> MatImages.toBufferedImage(source));
        } finally {
            source.release();
        }
    }
}
