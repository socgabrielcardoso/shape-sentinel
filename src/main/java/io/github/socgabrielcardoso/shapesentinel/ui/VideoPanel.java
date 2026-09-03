package io.github.socgabrielcardoso.shapesentinel.ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

final class VideoPanel extends JPanel {
    private volatile BufferedImage frame;

    VideoPanel() {
        setBackground(new Color(11, 14, 19));
    }

    void setFrame(BufferedImage frame) {
        this.frame = frame;
        repaint();
    }

    void clearFrame() {
        frame = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        BufferedImage current = frame;
        if (current == null) {
            paintWaitingState(canvas);
            canvas.dispose();
            return;
        }

        double scale = Math.min(
                getWidth() / (double) current.getWidth(),
                getHeight() / (double) current.getHeight()
        );
        int width = (int) Math.round(current.getWidth() * scale);
        int height = (int) Math.round(current.getHeight() * scale);
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        canvas.drawImage(current, x, y, width, height, null);
        canvas.dispose();
    }

    private void paintWaitingState(Graphics2D canvas) {
        String title = "SHAPE SENTINEL";
        String subtitle = "Waiting for camera";
        canvas.setFont(new Font("Segoe UI", Font.BOLD, 24));
        FontMetrics titleMetrics = canvas.getFontMetrics();
        canvas.setColor(new Color(78, 224, 181));
        canvas.drawString(title, (getWidth() - titleMetrics.stringWidth(title)) / 2, getHeight() / 2 - 6);

        canvas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        FontMetrics subtitleMetrics = canvas.getFontMetrics();
        canvas.setColor(new Color(132, 142, 158));
        canvas.drawString(
                subtitle,
                (getWidth() - subtitleMetrics.stringWidth(subtitle)) / 2,
                getHeight() / 2 + 24
        );
    }
}
