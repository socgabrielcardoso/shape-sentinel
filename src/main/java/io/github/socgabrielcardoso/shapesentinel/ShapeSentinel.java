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
        } catch (RuntimeException | LinkageError error) {
            SwingUtilities.invokeLater(() -> showStartupError(error));
        }
    }

    private static void showStartupError(Throwable error) {
        String detail = error.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = error.getClass().getSimpleName();
        }
        JOptionPane.showMessageDialog(
                null,
                "OpenCV could not be initialized: " + detail,
                "Shape Sentinel",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
