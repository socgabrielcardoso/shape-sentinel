package io.github.socgabrielcardoso.shapesentinel.camera;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Objects;

final class MatImages {
    private MatImages() {
    }

    static BufferedImage toBufferedImage(Mat source) {
        Objects.requireNonNull(source, "source");
        if (source.empty()) {
            throw new IllegalArgumentException("Source image must not be empty");
        }
        if (source.channels() != 1 && source.channels() != 3) {
            throw new IllegalArgumentException("Source image must have one or three channels");
        }

        int type = source.channels() == 1
                ? BufferedImage.TYPE_BYTE_GRAY
                : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(source.width(), source.height(), type);
        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        source.get(0, 0, target);
        return image;
    }
}
