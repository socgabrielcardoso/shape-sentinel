package io.github.socgabrielcardoso.shapesentinel.camera;

import io.github.socgabrielcardoso.shapesentinel.core.DetectionSettings;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CameraServiceTest {
    @Test
    void rejectsNegativeCameraIndex() {
        try (CameraService service = new CameraService(DetectionSettings::defaults, listener())) {
            assertThrows(IllegalArgumentException.class, () -> service.start(-1));
        }
    }

    @Test
    void requiresCollaborators() {
        assertThrows(NullPointerException.class, () -> new CameraService(null, listener()));
        assertThrows(
                NullPointerException.class,
                () -> new CameraService(DetectionSettings::defaults, null)
        );
    }

    private CameraService.FrameListener listener() {
        return new CameraService.FrameListener() {
            @Override
            public void onFrame(BufferedImage image, CameraService.FrameStats stats) {
            }

            @Override
            public void onState(String message, boolean active) {
            }
        };
    }
}
