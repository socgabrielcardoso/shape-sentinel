package io.github.socgabrielcardoso.shapesentinel.ui;

import io.github.socgabrielcardoso.shapesentinel.camera.CameraService;
import io.github.socgabrielcardoso.shapesentinel.core.DetectionSettings;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class MainWindow extends JFrame implements CameraService.FrameListener {
    private static final Color BACKGROUND = new Color(17, 21, 28);
    private static final Color PANEL = new Color(25, 30, 39);
    private static final Color TEXT = new Color(235, 239, 245);
    private static final Color MUTED = new Color(137, 147, 163);
    private static final Color ACCENT = new Color(78, 224, 181);

    private final AtomicReference<DetectionSettings> settings =
            new AtomicReference<>(DetectionSettings.defaults());
    private final AtomicReference<FrameUpdate> pendingFrame = new AtomicReference<>();
    private final AtomicBoolean frameUpdateScheduled = new AtomicBoolean();
    private final VideoPanel videoPanel = new VideoPanel();
    private final JLabel status = new JLabel("Initializing camera");
    private final JLabel areaValue = new JLabel("1200 px");
    private final JLabel edgeValue = new JLabel("70");
    private final JSpinner cameraIndex = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
    private final JButton cameraButton = new JButton("STOP CAMERA");
    private final CameraService cameraService = new CameraService(settings::get, this);

    public MainWindow() {
        super("Shape Sentinel");
        configureLookAndFeel();
        configureWindow();
        setContentPane(buildContent());
        bindLifecycle();
    }

    public void open() {
        setVisible(true);
        startCamera();
    }

    private void configureLookAndFeel() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("Slider.background", PANEL);
        UIManager.put("Slider.foreground", ACCENT);
        UIManager.put("Spinner.background", PANEL);
    }

    private void configureWindow() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        setSize(1120, 780);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BACKGROUND);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(videoPanel, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JLabel title = new JLabel("SHAPE SENTINEL");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel mode = new JLabel("REAL-TIME COMPUTER VISION");
        mode.setForeground(ACCENT);
        mode.setFont(new Font("Segoe UI", Font.BOLD, 11));

        header.add(title, BorderLayout.WEST);
        header.add(mode, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        controls.setOpaque(false);
        controls.add(label("CAMERA"));
        controls.add(cameraIndex);

        JSlider areaSlider = new JSlider(300, 8_000, 1_200);
        configureSlider(areaSlider, 140);
        controls.add(label("MIN AREA"));
        controls.add(areaSlider);
        controls.add(areaValue);

        JSlider edgeSlider = new JSlider(20, 180, 70);
        configureSlider(edgeSlider, 120);
        controls.add(label("SENSITIVITY"));
        controls.add(edgeSlider);
        controls.add(edgeValue);

        styleValue(areaValue);
        styleValue(edgeValue);
        styleButton(cameraButton);
        controls.add(cameraButton);

        status.setForeground(MUTED);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        areaSlider.addChangeListener(event -> updateSettings(areaSlider, edgeSlider));
        edgeSlider.addChangeListener(event -> updateSettings(areaSlider, edgeSlider));
        cameraButton.addActionListener(event -> toggleCamera());

        footer.add(controls, BorderLayout.CENTER);
        footer.add(status, BorderLayout.SOUTH);
        return footer;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return label;
    }

    private void configureSlider(JSlider slider, int width) {
        slider.setPreferredSize(new Dimension(width, 28));
        slider.setOpaque(false);
        slider.setFocusable(false);
    }

    private void styleValue(JLabel value) {
        value.setForeground(TEXT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private void styleButton(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(new Color(12, 31, 26));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
    }

    private void updateSettings(JSlider areaSlider, JSlider edgeSlider) {
        int area = areaSlider.getValue();
        int edge = edgeSlider.getValue();
        settings.set(new DetectionSettings(area, edge));
        areaValue.setText(area + " px");
        edgeValue.setText(Integer.toString(edge));
    }

    private void toggleCamera() {
        if (cameraService.isRunning()) {
            cameraService.stop();
            applyCameraState("Camera stopped", false);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        int index = (Integer) cameraIndex.getValue();
        applyCameraState("Connecting to camera " + index, true);
        cameraService.start(index);
    }

    private void applyCameraState(String message, boolean active) {
        status.setText(message);
        cameraButton.setText(active ? "STOP CAMERA" : "START CAMERA");
        cameraIndex.setEnabled(!active);
        if (!active) {
            pendingFrame.set(null);
            videoPanel.clearFrame();
        }
    }

    private void bindLifecycle() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                cameraService.close();
                dispose();
            }
        });
    }

    @Override
    public void onFrame(BufferedImage image, CameraService.FrameStats stats) {
        pendingFrame.set(new FrameUpdate(image, stats));
        scheduleFrameUpdate();
    }

    private void scheduleFrameUpdate() {
        if (frameUpdateScheduled.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(this::renderPendingFrame);
        }
    }

    private void renderPendingFrame() {
        FrameUpdate update = pendingFrame.getAndSet(null);
        if (update != null) {
            videoPanel.setFrame(update.image());
            CameraService.FrameStats stats = update.stats();
            status.setText(String.format(
                    Locale.ROOT,
                    "Camera %d  |  %dx%d  |  %.1f FPS  |  %d shapes",
                    stats.cameraIndex(),
                    stats.width(),
                    stats.height(),
                    stats.fps(),
                    stats.shapes()
            ));
        }
        frameUpdateScheduled.set(false);
        if (pendingFrame.get() != null) {
            scheduleFrameUpdate();
        }
    }

    @Override
    public void onState(String message, boolean active) {
        SwingUtilities.invokeLater(() -> applyCameraState(message, active));
    }

    private record FrameUpdate(BufferedImage image, CameraService.FrameStats stats) {
    }
}
