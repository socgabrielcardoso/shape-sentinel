package io.github.socgabrielcardoso.shapesentinel;

import io.github.socgabrielcardoso.shapesentinel.ui.MainWindow;
import nu.pattern.OpenCV;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class ShapeSentinel {
    private ShapeSentinel() {
    }

    public static void main(String[] args) {
        try {
            OpenCV.loadLocally();
            SwingUtilities.invokeLater(() -> new MainWindow().open());
        } catch (Throwable error) {
            JOptionPane.showMessageDialog(
                    null,
                    "OpenCV could not be initialized: " + error.getMessage(),
                    "Shape Sentinel",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
