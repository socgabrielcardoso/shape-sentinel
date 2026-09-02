package io.github.socgabrielcardoso.shapesentinel.camera;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

final class MatImages {
    private MatImages() {
    }

    static BufferedImage toBufferedImage(Mat source) {
        int type = source.channels() == 1
                ? BufferedImage.TYPE_BYTE_GRAY
                : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(source.width(), source.height(), type);
        byte[] target = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        source.get(0, 0, target);
        return image;
    }
}
