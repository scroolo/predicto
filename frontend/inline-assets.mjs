import { readFileSync, writeFileSync, readdirSync } from 'fs';
import { join } from 'path';

const distDir = 'dist';
let html = readFileSync(join(distDir, 'index.html'), 'utf-8');

// Find and inline CSS
const cssMatch = html.match(/href="([^"]+\.css)"/);
if (cssMatch) {
  const cssPath = join(distDir, cssMatch[1].replace(/^\//, ''));
  try {
    const css = readFileSync(cssPath, 'utf-8');
    html = html.replace(/<link[^>]+\.css[^>]+>/, `<style>${css}</style>`);
  } catch(e) { console.log('CSS not found:', cssPath); }
}

// Find and inline JS
const jsMatch = html.match(/src="([^"]+\.js)"/);
if (jsMatch) {
  const jsPath = join(distDir, jsMatch[1].replace(/^\//, ''));
  try {
    const js = readFileSync(jsPath, 'utf-8');
    html = html.replace(/<script[^>]+\.js[^>]+><\/script>/, `<script>${js}</script>`);
  } catch(e) { console.log('JS not found:', jsPath); }
}

writeFileSync(join(distDir, 'index.html'), html);
console.log('Assets inlined successfully');
