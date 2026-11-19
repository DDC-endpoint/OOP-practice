// Renders every drill LESSON.md → LESSON.pdf + HINTS.pdf, every PAPER.md → PAPER.pdf, and every guides/<NAME>.md → guides/<NAME>.pdf.
// Usage (from anywhere):  node scripts/pdf/build.mjs [filter]   e.g. `d01` or `P1` to build only matching folders.
// Prefer the wrappers: bash scripts/build-pdfs.sh  |  scripts\build-pdfs.cmd
import { readFileSync, existsSync } from 'node:fs';
import { join, basename } from 'node:path';
import { Marked } from 'marked';
import hljs from 'highlight.js';
import puppeteer from 'puppeteer-core';
import { ROOT, sources, parseDoc, splitDetails, findSection, hintTitle } from './lib.mjs';

// ---------- Markdown → HTML ----------
const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const md = new Marked({
  gfm: true,
  renderer: {
    code(token) {
      const text = typeof token === 'string' ? token : token.text;
      const lang = (typeof token === 'string' ? arguments[1] : token.lang || '').trim().split(/\s+/)[0].toLowerCase();
      const known = lang && hljs.getLanguage(lang);
      const body = known ? hljs.highlight(text, { language: lang, ignoreIllegals: true }).value : esc(text);
      const label = lang && lang !== 'text' ? `<span class="lang">${esc(lang)}</span>` : '';
      return `<pre class="code${label ? ' has-lang' : ''}">${label}<code class="hljs">${body}</code></pre>\n`;
    },
  },
});
const block = (s) => md.parse(s);
const inline = (s) => md.parseInline(s);

const cap = (s) => s.replace(/^(["'(]?)([a-z])/, (_, q, c) => q + c.toUpperCase());
const card = (n, title, body, cls) => `
<section class="card ${cls}">
  <header>${n != null ? `<span class="num">${n}</span>` : ''}<h3>${inline(cap(title))}</h3></header>
  <div class="card-body">${block(body)}</div>
</section>`;

/** Renders a section body, turning any <details> into an always-open card. */
const renderBody = (body, cls = 'note') =>
  splitDetails(body).map((p) => (p.type === 'prose' ? block(p.text) : card(null, p.summary, p.body, cls))).join('\n');

const dots = (n) => (n ? `<span class="dots">${[1, 2, 3, 4, 5].map((i) => `<i class="${i <= n ? 'on' : ''}"></i>`).join('')}</span>` : '');

function titleBlock(meta, h1, kicker) {
  const tasks = Array.isArray(meta.tasks) && meta.tasks.length
    ? `<table class="marks"><thead><tr><th>${meta.kind === 'drill' ? 'You practise' : 'Part'}</th><th>Marks</th></tr></thead><tbody>
       ${meta.tasks.map((t) => `<tr><td>${inline(String(t.name))}</td><td class="m">${t.marks ?? ''}</td></tr>`).join('')}
       <tr class="total"><td>Total</td><td class="m">${meta.tasks.reduce((a, t) => a + (Number(t.marks) || 0), 0)}</td></tr>
       </tbody></table>`
    : '';
  const facts = [
    meta.difficulty ? `<span>Difficulty ${dots(meta.difficulty)}</span>` : '',
    meta.timeMinutes ? `<span>⏱ ${meta.timeMinutes} min</span>` : '',
  ].filter(Boolean).join('');
  return `
<header class="title">
  <div class="kicker">${kicker}</div>
  <h1>${inline(h1)}</h1>
  ${facts ? `<div class="facts">${facts}</div>` : ''}
  ${tasks}
</header>`;
}

/** Pull the `# ` heading out of the preamble (the title block shows it instead). */
function takeH1(preamble, fallback) {
  const m = preamble.match(/^# (.+)$/m);
  return m ? { h1: m[1].trim(), rest: preamble.replace(m[0], '') } : { h1: fallback, rest: preamble };
}

function lessonHtml(doc, id) {
  const { h1, rest } = takeH1(doc.preamble, doc.meta.title ?? id);
  let out = titleBlock(doc.meta, h1, 'OOP drill · Lesson') + block(rest);
  for (const s of doc.sections) {
    const name = s.heading.trim().toLowerCase();
    if (name === 'hints') continue;                       // → HINTS.pdf
    if (name === 'viva questions') {
      const parts = splitDetails(s.body);
      const prose = parts.filter((p) => p.type === 'prose').map((p) => block(p.text)).join('');
      const qs = parts.filter((p) => p.type === 'card');
      out += `<h2>${inline(s.heading)}</h2>${prose}
        <p class="aside">Say each answer out loud first. Model answers are in <b>HINTS.pdf</b>, under <i>Viva answers</i>.</p>
        <ol class="viva">${qs.map((q) => `<li>${inline(q.summary)}</li>`).join('')}</ol>`;
      continue;
    }
    out += `<h2>${inline(s.heading)}</h2>` + renderBody(s.body);
    if (name === 'your task' && findSection(doc, 'Hints')) {
      out += `<p class="aside">Stuck? The hints are in <b>HINTS.pdf</b> next to this file. Open them one at a time.</p>`;
    }
  }
  return out;
}

function hintsHtml(doc, id) {
  const { h1 } = takeH1(doc.preamble, doc.meta.title ?? id);
  let out = titleBlock({ ...doc.meta, tasks: [], difficulty: 0, timeMinutes: 0 }, h1, 'OOP drill · Hints & viva answers');
  const hints = findSection(doc, 'Hints');
  if (hints) {
    const parts = splitDetails(hints.body);
    out += `<h2>Hints</h2><p class="aside">Read <b>one</b> hint, go back to the code, and only come back if you're still stuck. The last hint is close to the answer.</p>`;
    let n = 0;
    out += parts.map((p) => (p.type === 'prose' ? block(p.text) : card(++n, hintTitle(p.summary), p.body, 'hint'))).join('\n');
  }
  const viva = findSection(doc, 'Viva questions');
  if (viva) {
    const qs = splitDetails(viva.body).filter((p) => p.type === 'card');
    out += `<h2 class="new-page">Viva answers</h2><p class="aside">Answer out loud first, then compare. Explain it in your own words; don't recite these.</p>`;
    out += qs.map((q, i) => card(i + 1, q.summary, q.body, 'viva')).join('\n');
  }
  return out;
}

function paperHtml(doc) {
  let out = titleBlock(doc.meta, doc.meta.title ?? 'Paper', doc.meta.kind === 'mock-paper' ? 'OOP · Mock paper' : 'OOP · Practice paper');
  out += block(doc.preamble);
  for (const s of doc.sections) out += `<h2>${inline(s.heading)}</h2>` + renderBody(s.body);
  return out;
}

function guideHtml(doc) {
  let out = titleBlock(doc.meta, doc.meta.title ?? 'Guide', 'OOP · LBP guide');
  out += block(takeH1(doc.preamble, '').rest);
  for (const s of doc.sections) out += `<h2>${inline(s.heading)}</h2>` + renderBody(s.body);
  return out;
}

// ---------- HTML → PDF ----------
const CSS = readFileSync(new URL('./style.css', import.meta.url), 'utf8');
const HLJS_CSS = readFileSync(new URL('./node_modules/highlight.js/styles/github-dark.css', import.meta.url), 'utf8');
const page = (title, body) => `<!doctype html><html><head><meta charset="utf-8"><title>${esc(title)}</title>
<style>${HLJS_CSS}\n${CSS}</style></head><body><main>${body}</main></body></html>`;

const footer = (label) => `<div style="font-family:'Segoe UI',Arial,sans-serif;font-size:8px;color:#6b7280;width:100%;padding:0 16mm;display:flex;justify-content:space-between;">
  <span>${esc(label)}</span><span>page <span class="pageNumber"></span> of <span class="totalPages"></span></span></div>`;

function findBrowser() {
  const candidates = [
    process.env.CHROME_PATH,
    'C:/Program Files/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Google/Chrome/Application/chrome.exe',
    `${process.env.LOCALAPPDATA ?? ''}/Google/Chrome/Application/chrome.exe`,
    'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    'C:/Program Files/Microsoft/Edge/Application/msedge.exe',
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/usr/bin/google-chrome', '/usr/bin/chromium', '/usr/bin/chromium-browser', '/usr/bin/microsoft-edge',
  ].filter(Boolean);
  const hit = candidates.find((p) => existsSync(p));
  if (!hit) {
    console.error('No Chrome or Edge found. Set CHROME_PATH to your browser executable and re-run.');
    process.exit(2);
  }
  return hit;
}

async function render(browser, html, outRel, label) {
  const tab = await browser.newPage();
  await tab.setContent(html, { waitUntil: 'load' });
  await tab.pdf({
    path: join(ROOT, outRel),
    format: 'A4',
    printBackground: true,
    displayHeaderFooter: true,
    headerTemplate: '<div></div>',
    footerTemplate: footer(label),
    margin: { top: '16mm', bottom: '18mm', left: '16mm', right: '16mm' },
  });
  await tab.close();
  console.log(`  ${outRel.replace(/\\/g, '/')}`);
}

const filter = (process.argv[2] ?? '').toLowerCase();
const todo = sources().filter((s) => !filter || s.dir.toLowerCase().includes(filter));
if (!todo.length) { console.error(`Nothing matches "${filter}".`); process.exit(1); }

const started = Date.now();
const browser = await puppeteer.launch({ executablePath: findBrowser(), headless: true });
try {
  for (const s of todo) {
    const doc = parseDoc(s.src);
    const id = String(doc.meta.id ?? basename(s.dir)).toUpperCase().replace(/^D/, 'd');
    if (s.kind === 'drill') {
      await render(browser, page(`${id} · Lesson`, lessonHtml(doc, id)), join(s.dir, 'LESSON.pdf'), `${id} · Lesson · ${doc.meta.title ?? ''}`);
      await render(browser, page(`${id} · Hints`, hintsHtml(doc, id)), join(s.dir, 'HINTS.pdf'), `${id} · Hints & viva answers`);
    } else if (s.kind === 'guide') {
      const title = String(doc.meta.title ?? id);
      await render(browser, page(title, guideHtml(doc)), join(s.dir, s.out), title);
    } else {
      await render(browser, page(`${id} · Paper`, paperHtml(doc)), join(s.dir, 'PAPER.pdf'), String(doc.meta.title ?? id));
    }
  }
} finally {
  await browser.close();
}
console.log(`Built ${todo.reduce((n, s) => n + (s.kind === 'drill' ? 2 : 1), 0)} PDFs in ${((Date.now() - started) / 1000).toFixed(1)}s.`);
