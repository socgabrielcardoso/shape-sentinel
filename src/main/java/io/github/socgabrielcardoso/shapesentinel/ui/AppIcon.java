package io.github.socgabrielcardoso.shapesentinel.ui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

final class AppIcon {
    private AppIcon() {
    }

    static Image create() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setColor(AppTheme.SURFACE);
        canvas.fillRoundRect(2, 2, 60, 60, 16, 16);
        canvas.setColor(AppTheme.ACCENT);
        canvas.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D triangle = new Path2D.Double();
        triangle.moveTo(32, 14);
        triangle.lineTo(50, 47);
        triangle.lineTo(14, 47);
        triangle.closePath();
        canvas.draw(triangle);
        canvas.dispose();
        return image;
    }
}
