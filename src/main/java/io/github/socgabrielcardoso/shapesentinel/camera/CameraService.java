package io.github.socgabrielcardoso.shapesentinel.camera;

import io.github.socgabrielcardoso.shapesentinel.core.DetectionSettings;
import io.github.socgabrielcardoso.shapesentinel.core.FrameProcessor;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public final class CameraService implements AutoCloseable {
    private final Supplier<DetectionSettings> settingsSupplier;
    private final FrameListener listener;

    private volatile boolean running;
    private volatile VideoCapture activeCapture;
    private volatile long sessionId;
    private ExecutorService executor;

    public CameraService(Supplier<DetectionSettings> settingsSupplier, FrameListener listener) {
        this.settingsSupplier = Objects.requireNonNull(settingsSupplier, "settingsSupplier");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public synchronized void start(int cameraIndex) {
        if (cameraIndex < 0) {
            throw new IllegalArgumentException("Camera index must not be negative");
        }
        stop();
        long session = ++sessionId;
        running = true;
        ThreadFactory factory = task -> {
            Thread thread = new Thread(task, "shape-sentinel-camera");
            thread.setDaemon(true);
            return thread;
        };
        executor = Executors.newSingleThreadExecutor(factory);
        executor.submit(() -> capture(cameraIndex, session));
    }

    public synchronized void stop() {
        sessionId++;
        running = false;
        VideoCapture capture = activeCapture;
        if (capture != null) {
            capture.release();
            activeCapture = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void capture(int cameraIndex, long session) {
        VideoCapture camera = new VideoCapture();
        Mat frame = new Mat();

        try {
            if (!registerCapture(camera, session)) {
                return;
            }

            try (FrameProcessor processor = new FrameProcessor()) {
                if (!camera.open(cameraIndex)) {
                    publishState(session, "Camera " + cameraIndex + " is unavailable", false);
                    return;
                }

                camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
                camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);
                publishState(session, "Camera " + cameraIndex + " connected", true);

                long windowStart = System.nanoTime();
                int windowFrames = 0;
                double fps = 0;

                while (running && session == sessionId && !Thread.currentThread().isInterrupted()) {
                    if (!camera.read(frame) || frame.empty()) {
                        publishState(session, "Camera stream interrupted", false);
                        break;
                    }

                    windowFrames++;
                    long elapsed = System.nanoTime() - windowStart;
                    if (elapsed >= 500_000_000L) {
                        fps = windowFrames * 1_000_000_000.0 / elapsed;
                        windowFrames = 0;
                        windowStart = System.nanoTime();
                    }

                    int shapes = processor.process(frame, settingsSupplier.get(), fps);
                    BufferedImage image = MatImages.toBufferedImage(frame);
                    publishFrame(
                            session,
                            image,
                            new FrameStats(cameraIndex, frame.width(), frame.height(), shapes, fps)
                    );
                }
            }
        } catch (RuntimeException error) {
            publishState(session, "Camera error: " + error.getMessage(), false);
        } finally {
            frame.release();
            camera.release();
            finishCapture(camera, session);
        }
    }

    private synchronized boolean registerCapture(VideoCapture camera, long session) {
        if (!running || session != sessionId) {
            return false;
        }
        activeCapture = camera;
        return true;
    }

    private synchronized void finishCapture(VideoCapture camera, long session) {
        if (activeCapture == camera) {
            activeCapture = null;
        }
        if (session == sessionId) {
            running = false;
        }
    }

    private void publishFrame(long session, BufferedImage image, FrameStats stats) {
        if (running && session == sessionId) {
            listener.onFrame(image, stats);
        }
    }

    private void publishState(long session, String message, boolean active) {
        if (running && session == sessionId) {
            listener.onState(message, active);
        }
    }

    @Override
    public void close() {
        stop();
    }

    public interface FrameListener {
        void onFrame(BufferedImage image, FrameStats stats);

        void onState(String message, boolean active);
    }

    public record FrameStats(int cameraIndex, int width, int height, int shapes, double fps) {
    }
}
