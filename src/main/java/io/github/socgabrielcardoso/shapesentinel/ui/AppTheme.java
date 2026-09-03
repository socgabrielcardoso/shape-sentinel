package io.github.socgabrielcardoso.shapesentinel.ui;

import java.awt.Color;
import java.awt.Font;

final class AppTheme {
    static final Color BACKGROUND = new Color(17, 21, 28);
    static final Color SURFACE = new Color(25, 30, 39);
    static final Color VIDEO_BACKGROUND = new Color(11, 14, 19);
    static final Color TEXT = new Color(235, 239, 245);
    static final Color MUTED = new Color(137, 147, 163);
    static final Color ACCENT = new Color(78, 224, 181);
    static final Color ACCENT_INK = new Color(12, 31, 26);

    private AppTheme() {
    }

    static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }
}
