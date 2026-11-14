// Serves a built Snake gdx-teavm webapp, opens it in headless Chrome, waits for a canvas and a
// document.title matching "Snake — score: <digits>", and fails closed otherwise.
// Usage: node check.mjs <webapp-dir>
// Env: CHROME=<path to Chrome>; otherwise Playwright's "chrome" channel is used.
import { createServer } from 'node:http';
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { extname, join, normalize, resolve } from 'node:path';
import { chromium } from 'playwright-core';

const TYPES = {
  '.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript', '.wasm': 'application/wasm',
  '.png': 'image/png', '.json': 'application/json', '.txt': 'text/plain', '.fnt': 'text/plain',
  '.glsl': 'text/plain', '.map': 'application/json',
};

const TITLE_RE = /^Snake — score: \d+$/;
const TIMEOUT_MS = 60_000;

function serve(root) {
  const server = createServer(async (req, res) => {
    const path = normalize(decodeURIComponent(new URL(req.url, 'http://x').pathname)).replace(/^([/\\])+/, '');
    const file = join(root, path === '' ? 'index.html' : path);
    if (!file.startsWith(root)) { res.writeHead(403).end(); return; }
    try {
      const body = await readFile(file);
      res.writeHead(200, { 'content-type': TYPES[extname(file)] ?? 'application/octet-stream' }).end(body);
    } catch {
      res.writeHead(404).end();
    }
  });
  return new Promise((ok) => server.listen(0, '127.0.0.1', () => ok(server)));
}

async function check(browser, dir) {
  const root = resolve(dir);
  const server = await serve(root);
  const url = `http://127.0.0.1:${server.address().port}/index.html`;
  const page = await browser.newPage();
  const logs = [];
  page.on('console', (m) => logs.push(`[console.${m.type()}] ${m.text()}`));
  page.on('pageerror', (e) => logs.push(`[pageerror] ${e.message}`));
  try {
    await page.goto(url);
    await page.locator('canvas').first().waitFor({ timeout: TIMEOUT_MS });
    await page.waitForFunction(
      (pattern) => new RegExp(pattern).test(document.title),
      TITLE_RE.source,
      { timeout: TIMEOUT_MS },
    );
    const title = await page.title();
    if (!TITLE_RE.test(title)) {
      throw new Error(`unexpected title ${JSON.stringify(title)}`);
    }
    await mkdir('build', { recursive: true });
    const name = dir.replace(/[^a-z0-9]+/gi, '_');
    await writeFile(`build/${name}.txt`, `title: ${title}\n--- console ---\n${logs.join('\n')}\n`);
    await page.screenshot({ path: `build/${name}.png`, fullPage: true });
    console.log(`=== ${dir} (title: ${title}) ===`);
  } catch (e) {
    console.error(`FAIL ${dir}: ${e.message}\n${logs.join('\n')}`);
    throw e;
  } finally {
    await page.close();
    server.close();
  }
}

const dirs = process.argv.slice(2);
if (!dirs.length) { console.error('usage: node check.mjs <webapp-dir>'); process.exit(2); }
const launch = process.env.CHROME ? { executablePath: process.env.CHROME } : { channel: 'chrome' };
const browser = await chromium.launch({
  ...launch, headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'],
});
let failed = false;
for (const dir of dirs) {
  try { await check(browser, dir); } catch { failed = true; }
}
await browser.close();
if (failed) process.exit(1);
console.log(`Browser check passed for ${dirs.length} webapp(s).`);
