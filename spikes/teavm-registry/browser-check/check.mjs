// Serves a built gdx-teavm webapp, opens it in headless Chrome, waits for the TeaVM-compiled program to
// write its report into #ws-scene, and fails unless the report says PASS and shows the decoded values.
// Usage: node check.mjs <webapp-dir> [<webapp-dir> ...]
// Env: CHROME=<path to Chrome>; otherwise Playwright's "chrome" channel (the installed Google Chrome) is used.
import { createServer } from 'node:http';
import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { extname, join, normalize, resolve } from 'node:path';
import { chromium } from 'playwright-core';

const TYPES = {
  '.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript', '.wasm': 'application/wasm',
  '.png': 'image/png', '.json': 'application/json', '.txt': 'text/plain', '.fnt': 'text/plain',
  '.glsl': 'text/plain', '.map': 'application/json',
};

const EXPECTED = [
  'WS-S2-RESULT: PASS',
  'Transform2D x=12.5 y=-3.25 rotation=90',
  'Sprite texture=hero.png layer=2 alpha=0.75 visible=true',
  'Shadow',
  'Sprite texture=shadow.png layer=1 alpha=0.5 visible=false',
  'Ground',
  'control: reflective Json failed as expected',
];

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
    const pre = page.locator('#ws-scene');
    await pre.waitFor({ timeout: 60_000 });
    const report = await pre.innerText();
    const title = await page.title();
    await mkdir('build', { recursive: true });
    const name = dir.replace(/[^a-z0-9]+/gi, '_');
    await writeFile(`build/${name}.txt`, `title: ${title}\n${report}\n--- console ---\n${logs.join('\n')}\n`);
    await page.screenshot({ path: `build/${name}.png`, fullPage: true });
    console.log(`=== ${dir} (title: ${title}) ===\n${report}`);
    const missing = EXPECTED.filter((s) => !report.includes(s));
    if (missing.length) throw new Error(`report is missing: ${missing.join(' | ')}`);
    if (title !== 'WS-S2 PASS') throw new Error(`unexpected title ${title}`);
  } catch (e) {
    console.error(`FAIL ${dir}: ${e.message}\n${logs.join('\n')}`);
    throw e;
  } finally {
    await page.close();
    server.close();
  }
}

const dirs = process.argv.slice(2);
if (!dirs.length) { console.error('usage: node check.mjs <webapp-dir>...'); process.exit(2); }
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
