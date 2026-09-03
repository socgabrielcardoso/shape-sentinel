const cameraButton = document.querySelector("#camera-toggle");
const cameraSource = document.querySelector("#camera-source");
const feedState = document.querySelector("#feed-state");
const previewCanvas = document.querySelector("#detection-preview");

let cameraStream = null;
let detectorFrame = 0;
let detectorResources = null;
let openCvPromise = null;

function setCameraStatus(message, buttonText, disabled = false) {
    feedState.textContent = message;
    cameraButton.textContent = buttonText;
    cameraButton.disabled = disabled;
}

function loadOpenCv() {
    if (window.cv?.Mat) {
        return Promise.resolve(window.cv);
    }
    if (openCvPromise) {
        return openCvPromise;
    }

    openCvPromise = new Promise((resolve, reject) => {
        const timeout = window.setTimeout(
                () => reject(new Error("Detection engine timed out")),
                30_000
        );
        const previousModule = window.Module || {};
        window.Module = {
            ...previousModule,
            onRuntimeInitialized() {
                window.clearTimeout(timeout);
                previousModule.onRuntimeInitialized?.();
                resolve(window.cv);
            }
        };

        const script = document.createElement("script");
        script.src = "https://docs.opencv.org/4.9.0/opencv.js";
        script.async = true;
        script.addEventListener("error", () => {
            window.clearTimeout(timeout);
            reject(new Error("Detection engine could not be loaded"));
        });
        script.addEventListener("load", async () => {
            const candidate = await window.cv;
            if (candidate?.Mat) {
                window.clearTimeout(timeout);
                window.cv = candidate;
                resolve(candidate);
            }
        });
        document.head.append(script);
    });

    return openCvPromise;
}

function createDetectorResources(cv) {
    const width = 640;
    const height = 400;
    cameraSource.width = width;
    cameraSource.height = height;
    previewCanvas.width = width;
    previewCanvas.height = height;

    return {
        cv,
        capture: new cv.VideoCapture(cameraSource),
        source: new cv.Mat(height, width, cv.CV_8UC4),
        gray: new cv.Mat(),
        blurred: new cv.Mat(),
        edges: new cv.Mat(),
        hierarchy: new cv.Mat(),
        contours: new cv.MatVector(),
        kernel: cv.Mat.ones(3, 3, cv.CV_8U),
        width,
        height,
        startedAt: performance.now(),
        frames: 0
    };
}

function classifyContour(cv, contour, approximation, area, perimeter) {
    const vertices = approximation.rows;
    const convex = cv.isContourConvex(approximation);
    if (!convex) {
        return { type: "POLYGON", confidence: 68, color: [218, 218, 218, 255] };
    }

    if (vertices === 3) {
        return { type: "TRIANGLE", confidence: 94, color: [255, 214, 80, 255] };
    }
    if (vertices === 4) {
        const bounds = cv.boundingRect(contour);
        const ratio = Math.min(bounds.width, bounds.height) / Math.max(bounds.width, bounds.height);
        if (ratio >= 0.86) {
            return { type: "SQUARE", confidence: Math.round(82 + ratio * 16), color: [123, 234, 93, 255] };
        }
        return { type: "RECTANGLE", confidence: Math.round(88 + ratio * 8), color: [92, 181, 255, 255] };
    }
    if (vertices === 5) {
        return { type: "PENTAGON", confidence: 92, color: [255, 132, 226, 255] };
    }
    if (vertices === 6) {
        return { type: "HEXAGON", confidence: 91, color: [145, 111, 255, 255] };
    }

    const circularity = 4 * Math.PI * area / (perimeter * perimeter);
    if (circularity >= 0.76) {
        return { type: "CIRCLE", confidence: Math.round(78 + Math.min(1, circularity) * 20), color: [184, 224, 84, 255] };
    }
    return { type: "POLYGON", confidence: 72, color: [218, 218, 218, 255] };
}

function drawDetection(cv, source, contour, assessment, area) {
    const bounds = cv.boundingRect(contour);
    const color = new cv.Scalar(...assessment.color);
    const contours = new cv.MatVector();
    contours.push_back(contour);
    cv.drawContours(source, contours, 0, color, 3, cv.LINE_AA);
    contours.delete();

    const label = `${assessment.type}  ${Math.round(area)} px  ${assessment.confidence}%`;
    const x = Math.max(4, bounds.x);
    const y = bounds.y > 30 ? bounds.y - 8 : bounds.y + 24;
    cv.rectangle(
            source,
            new cv.Point(x - 4, y - 20),
            new cv.Point(Math.min(source.cols - 1, x + label.length * 8), y + 6),
            new cv.Scalar(36, 28, 24, 255),
            cv.FILLED
    );
    cv.putText(
            source,
            label,
            new cv.Point(x, y),
            cv.FONT_HERSHEY_SIMPLEX,
            0.46,
            new cv.Scalar(245, 239, 235, 255),
            1,
            cv.LINE_AA
    );
}

function processCameraFrame() {
    if (!cameraStream || !detectorResources || document.hidden) {
        return;
    }

    const resources = detectorResources;
    const cv = resources.cv;
    resources.capture.read(resources.source);
    cv.cvtColor(resources.source, resources.gray, cv.COLOR_RGBA2GRAY);
    cv.GaussianBlur(resources.gray, resources.blurred, new cv.Size(5, 5), 0, 0, cv.BORDER_DEFAULT);
    cv.Canny(resources.blurred, resources.edges, 70, 168);
    cv.morphologyEx(resources.edges, resources.edges, cv.MORPH_CLOSE, resources.kernel);
    cv.findContours(
            resources.edges,
            resources.contours,
            resources.hierarchy,
            cv.RETR_EXTERNAL,
            cv.CHAIN_APPROX_SIMPLE
    );

    let detected = 0;
    const maximumArea = resources.width * resources.height * 0.95;
    for (let index = 0; index < resources.contours.size(); index++) {
        const contour = resources.contours.get(index);
        const approximation = new cv.Mat();
        try {
            const area = cv.contourArea(contour);
            if (area < 900 || area > maximumArea) {
                continue;
            }

            const perimeter = cv.arcLength(contour, true);
            cv.approxPolyDP(contour, approximation, perimeter * 0.025, true);
            const assessment = classifyContour(cv, contour, approximation, area, perimeter);
            drawDetection(cv, resources.source, contour, assessment, area);
            detected++;
        } finally {
            approximation.delete();
            contour.delete();
        }
    }

    resources.frames++;
    const elapsed = Math.max(1, performance.now() - resources.startedAt);
    const fps = resources.frames * 1000 / elapsed;
    cv.rectangle(
            resources.source,
            new cv.Point(16, 16),
            new cv.Point(218, 50),
            new cv.Scalar(36, 28, 24, 255),
            cv.FILLED
    );
    cv.putText(
            resources.source,
            `SHAPES ${String(detected).padStart(2, "0")}   FPS ${fps.toFixed(1)}`,
            new cv.Point(28, 39),
            cv.FONT_HERSHEY_SIMPLEX,
            0.52,
            new cv.Scalar(181, 224, 78, 255),
            1,
            cv.LINE_AA
    );
    cv.imshow(previewCanvas, resources.source);
    detectorFrame = requestAnimationFrame(processCameraFrame);
}

function releaseDetector() {
    if (!detectorResources) {
        return;
    }
    cancelAnimationFrame(detectorFrame);
    detectorResources.source.delete();
    detectorResources.gray.delete();
    detectorResources.blurred.delete();
    detectorResources.edges.delete();
    detectorResources.hierarchy.delete();
    detectorResources.contours.delete();
    detectorResources.kernel.delete();
    detectorResources = null;
}

function stopCamera() {
    releaseDetector();
    cameraStream?.getTracks().forEach(track => track.stop());
    cameraStream = null;
    cameraSource.srcObject = null;
    setCameraStatus("SIMULATED FEED", "USE CAMERA");
    document.dispatchEvent(new Event("camera:stop"));
}

function cameraErrorMessage(error) {
    if (error?.name === "NotAllowedError") {
        return "CAMERA BLOCKED";
    }
    if (error?.name === "NotFoundError") {
        return "NO CAMERA FOUND";
    }
    return "CAMERA ERROR";
}

async function startCamera() {
    if (!navigator.mediaDevices?.getUserMedia) {
        setCameraStatus("CAMERA UNSUPPORTED", "USE CAMERA");
        return;
    }

    setCameraStatus("REQUESTING CAMERA", "WAIT", true);
    try {
        cameraStream = await navigator.mediaDevices.getUserMedia({
            audio: false,
            video: {
                facingMode: "environment",
                width: { ideal: 1280 },
                height: { ideal: 800 }
            }
        });
        cameraSource.srcObject = cameraStream;
        await cameraSource.play();
        setCameraStatus("LOADING DETECTOR", "WAIT", true);
        const cv = await loadOpenCv();
        detectorResources = createDetectorResources(cv);
        document.dispatchEvent(new Event("camera:start"));
        setCameraStatus("LIVE CAMERA", "STOP CAMERA");
        processCameraFrame();
    } catch (error) {
        releaseDetector();
        openCvPromise = null;
        cameraStream?.getTracks().forEach(track => track.stop());
        cameraStream = null;
        cameraSource.srcObject = null;
        setCameraStatus(cameraErrorMessage(error), "TRY AGAIN");
        document.dispatchEvent(new Event("camera:stop"));
    }
}

cameraButton.addEventListener("click", () => {
    if (cameraStream) {
        stopCamera();
    } else {
        startCamera();
    }
});

document.addEventListener("visibilitychange", () => {
    if (!cameraStream) {
        return;
    }
    cancelAnimationFrame(detectorFrame);
    if (!document.hidden) {
        detectorFrame = requestAnimationFrame(processCameraFrame);
    }
});

window.addEventListener("pagehide", stopCamera);
