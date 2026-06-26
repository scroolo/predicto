import { readFileSync, writeFileSync, readdirSync } from 'fs';
import { join } from 'path';

const distDir = 'dist';
let html = readFileSync(join(distDir, 'index.html'), 'utf-8');

// Inline all CSS files
const cssRegex = /<link[^>]+rel="stylesheet"[^>]+href="([^"]+)"[^>]*\/?>/g;
let match;
while ((match = cssRegex.exec(html)) !== null) {
  const cssUrl = match[1];
  const cssPath = join(distDir, cssUrl.replace(/^\//, ''));
  try {
    const css = readFileSync(cssPath, 'utf-8');
    html = html.replace(match[0], `<style>${css}</style>`);
    console.log('Inlined CSS:', cssUrl);
  } catch(e) { console.log('CSS not found:', cssPath, e.message); }
}

// Inline all JS module scripts
const jsRegex = /<script[^>]+type="module"[^>]+src="([^"]+)"[^>]*><\/script>/g;
while ((match = jsRegex.exec(html)) !== null) {
  const jsUrl = match[1];
  const jsPath = join(distDir, jsUrl.replace(/^\//, ''));
  try {
    const js = readFileSync(jsPath, 'utf-8');
    html = html.replace(match[0], `<script type="module">${js}</script>`);
    console.log('Inlined JS:', jsUrl);
  } catch(e) { console.log('JS not found:', jsPath, e.message); }
}

writeFileSync(join(distDir, 'index.html'), html);
console.log('Done! index.html size:', html.length, 'bytes');
