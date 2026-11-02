import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { runInNewContext } from 'node:vm';

const source = readFileSync(new URL('../camera.js', import.meta.url), 'utf8');

function mountCamera({ getUserMedia, play = async () => {} }) {
  const handlers = {};
  const events = [];
  const button = {
    disabled: false, textContent: '',
    addEventListener(type, fn) { handlers['button:' + type] = fn; }
  };
  const video = { srcObject: null, play };
  const status = { textContent: '' };
  const document = {
    hidden: false,
    querySelector(selector) {
      return ({
        '#camera-toggle': button, '#camera-source': video,
        '#feed-state': status, '#detection-preview': {}
      })[selector];
    },
    addEventListener(type, fn) { handlers['document:' + type] = fn; },
    dispatchEvent(event) { events.push(event.type); }
  };
  const window = {
    addEventListener(type, fn) { handlers['window:' + type] = fn; }
  };
  runInNewContext(source, {
    document, window, navigator: { mediaDevices: { getUserMedia } },
    Event: class { constructor(type) { this.type = type; } },
    cancelAnimationFrame() {}, requestAnimationFrame() { return 1; },
    performance: { now: () => 0 }
  });
  return { button, video, status, events, click: () => handlers['button:click'](),
    pagehide: () => handlers['window:pagehide']() };
}

test('closing page during pending camera permission releases late media stream', async () => {
  let resolvePermission;
  let stopped = 0;
  const stream = { getTracks: () => [{ stop: () => stopped++ }] };
  const camera = mountCamera({
    getUserMedia: () => new Promise(resolve => { resolvePermission = resolve; })
  });

  camera.click();
  assert.equal(camera.button.disabled, true);
  camera.pagehide();
  resolvePermission(stream);
  await Promise.resolve();
  await Promise.resolve();

  assert.equal(stopped, 1);
  assert.equal(camera.video.srcObject, null);
  assert.equal(camera.button.textContent, 'USE CAMERA');
  assert.deepEqual(camera.events, ['camera:stop']);
});

test('closing page during video play does not restart the detector', async () => {
  let resolvePlay;
  let stopped = 0;
  const stream = { getTracks: () => [{ stop: () => stopped++ }] };
  const camera = mountCamera({
    getUserMedia: async () => stream,
    play: () => new Promise(resolve => { resolvePlay = resolve; })
  });

  camera.click();
  await Promise.resolve();
  assert.equal(camera.video.srcObject, stream);
  camera.pagehide();
  resolvePlay();
  await Promise.resolve();
  await Promise.resolve();

  assert.equal(stopped, 1);
  assert.equal(camera.video.srcObject, null);
  assert.equal(camera.button.textContent, 'USE CAMERA');
  assert.deepEqual(camera.events, ['camera:stop']);
});
