const canvas = document.querySelector("#detection-preview");
const context = canvas.getContext("2d");
const motionPreference = window.matchMedia("(prefers-reduced-motion: reduce)");

const shapes = [
    { type: "TRIANGLE", x: 0.22, y: 0.34, vx: 0.035, vy: 0.025, size: 0.1, sides: 3, color: "#ffd650", rotation: 0.1 },
    { type: "SQUARE", x: 0.7, y: 0.3, vx: -0.028, vy: 0.032, size: 0.09, sides: 4, color: "#5cea7b", rotation: 0.18 },
    { type: "CIRCLE", x: 0.48, y: 0.72, vx: 0.03, vy: -0.027, size: 0.085, sides: 0, color: "#4ee0b5", rotation: 0 }
];

let width = 720;
let height = 450;
let animationFrame = 0;
let lastTime = performance.now();
let cameraActive = false;

function resizeCanvas() {
    const bounds = canvas.getBoundingClientRect();
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    width = Math.max(1, bounds.width);
    height = Math.max(1, bounds.height);
    canvas.width = Math.round(width * ratio);
    canvas.height = Math.round(height * ratio);
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    drawFrame(0);
}

function updateShapes(elapsed) {
    for (const shape of shapes) {
        shape.x += shape.vx * elapsed;
        shape.y += shape.vy * elapsed;
        shape.rotation += elapsed * 0.08;

        const horizontalLimit = shape.size * height / width;
        if (shape.x < horizontalLimit || shape.x > 1 - horizontalLimit) {
            shape.vx *= -1;
            shape.x = Math.max(horizontalLimit, Math.min(1 - horizontalLimit, shape.x));
        }
        if (shape.y < shape.size || shape.y > 1 - shape.size) {
            shape.vy *= -1;
            shape.y = Math.max(shape.size, Math.min(1 - shape.size, shape.y));
        }
    }
}

function drawGrid() {
    context.strokeStyle = "rgba(137, 147, 163, 0.08)";
    context.lineWidth = 1;
    const spacing = 32;
    for (let x = spacing; x < width; x += spacing) {
        context.beginPath();
        context.moveTo(x, 0);
        context.lineTo(x, height);
        context.stroke();
    }
    for (let y = spacing; y < height; y += spacing) {
        context.beginPath();
        context.moveTo(0, y);
        context.lineTo(width, y);
        context.stroke();
    }
}

function drawPolygon(shape, centerX, centerY, radius) {
    context.beginPath();
    for (let point = 0; point < shape.sides; point++) {
        const angle = shape.rotation - Math.PI / 2 + point * Math.PI * 2 / shape.sides;
        const x = centerX + Math.cos(angle) * radius;
        const y = centerY + Math.sin(angle) * radius;
        if (point === 0) {
            context.moveTo(x, y);
        } else {
            context.lineTo(x, y);
        }
    }
    context.closePath();
    context.stroke();
}

function drawLabel(shape, centerX, centerY, radius) {
    const label = `${shape.type}  ${Math.round(90 + shape.x * 8)}%`;
    context.font = "600 12px Consolas, monospace";
    const labelWidth = context.measureText(label).width + 16;
    const x = Math.max(8, Math.min(width - labelWidth - 8, centerX - radius));
    const y = Math.max(26, centerY - radius - 12);
    context.fillStyle = "rgba(25, 30, 39, 0.94)";
    context.fillRect(x, y - 20, labelWidth, 25);
    context.fillStyle = "#ebeff5";
    context.fillText(label, x + 8, y - 3);
}

function drawShape(shape) {
    const centerX = shape.x * width;
    const centerY = shape.y * height;
    const radius = shape.size * height;
    context.strokeStyle = shape.color;
    context.lineWidth = 3;

    if (shape.sides === 0) {
        context.beginPath();
        context.arc(centerX, centerY, radius, 0, Math.PI * 2);
        context.stroke();
    } else {
        drawPolygon(shape, centerX, centerY, radius);
    }

    drawLabel(shape, centerX, centerY, radius);
}

function drawMetrics() {
    const metrics = `SHAPES 03   FPS ${motionPreference.matches ? "00.0" : "60.0"}`;
    context.font = "600 12px Consolas, monospace";
    context.fillStyle = "rgba(25, 30, 39, 0.94)";
    context.fillRect(16, 16, 184, 34);
    context.fillStyle = "#4ee0b5";
    context.fillText(metrics, 28, 38);
}

function drawFrame(elapsed) {
    context.clearRect(0, 0, width, height);
    context.fillStyle = "#080b0f";
    context.fillRect(0, 0, width, height);
    drawGrid();
    if (elapsed > 0) {
        updateShapes(elapsed);
    }
    shapes.forEach(drawShape);
    drawMetrics();
}

function animate(timestamp) {
    const elapsed = Math.min((timestamp - lastTime) / 1000, 0.05);
    lastTime = timestamp;
    drawFrame(elapsed);
    animationFrame = requestAnimationFrame(animate);
}

function syncAnimation() {
    cancelAnimationFrame(animationFrame);
    lastTime = performance.now();
    if (cameraActive) {
        return;
    }
    if (motionPreference.matches || document.hidden) {
        drawFrame(0);
    } else {
        animationFrame = requestAnimationFrame(animate);
    }
}

if ("ResizeObserver" in window) {
    new ResizeObserver(resizeCanvas).observe(canvas);
} else {
    window.addEventListener("resize", resizeCanvas);
}

motionPreference.addEventListener("change", syncAnimation);
document.addEventListener("visibilitychange", syncAnimation);
document.addEventListener("camera:start", () => {
    cameraActive = true;
    cancelAnimationFrame(animationFrame);
});
document.addEventListener("camera:stop", () => {
    cameraActive = false;
    resizeCanvas();
    syncAnimation();
});
resizeCanvas();
syncAnimation();
