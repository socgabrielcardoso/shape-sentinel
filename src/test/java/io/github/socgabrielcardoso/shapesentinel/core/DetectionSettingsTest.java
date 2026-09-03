package io.github.socgabrielcardoso.shapesentinel.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DetectionSettingsTest {
    @Test
    void providesStableDefaults() {
        DetectionSettings settings = DetectionSettings.defaults();

        assertEquals(1_200, settings.minimumArea());
        assertEquals(70, settings.edgeThreshold());
        assertEquals(168, settings.highThreshold());
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new DetectionSettings(99, 70));
        assertThrows(IllegalArgumentException.class, () -> new DetectionSettings(1_200, 251));
        assertThrows(IllegalArgumentException.class, () -> new DetectionSettings(Double.NaN, 70));
    }
}
