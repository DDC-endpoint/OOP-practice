// Shared parsing for build.mjs and check.mjs, so both read the Markdown the same way.
import { readFileSync, readdirSync, existsSync } from 'node:fs';
import { join, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { load as loadYaml } from 'js-yaml';
import { marked } from 'marked';

export const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..');

/** Every source file that gets PDFs, with where its outputs go. */
export function sources() {
  const out = [];
  const dirs = (p) => existsSync(join(ROOT, p))
    ? readdirSync(join(ROOT, p), { withFileTypes: true }).filter((d) => d.isDirectory()).map((d) => join(p, d.name)).sort()
    : [];
  for (const d of dirs('drills')) {
    if (existsSync(join(ROOT, d, 'LESSON.md'))) out.push({ kind: 'drill', dir: d, src: join(d, 'LESSON.md') });
  }
  for (const d of [...dirs('past-papers'), ...dirs('mock-papers')]) {
    if (existsSync(join(ROOT, d, 'PAPER.md'))) out.push({ kind: 'paper', dir: d, src: join(d, 'PAPER.md') });
  }
  return out;
}

/** Read a file: front matter (YAML), the preamble before the first `## `, and the `## ` sections. */
export function parseDoc(relPath) {
  let text = readFileSync(join(ROOT, relPath), 'utf8').replace(/\r\n/g, '\n');
  let meta = {};
  const fm = text.match(/^---\n([\s\S]*?)\n---\n/);
  if (fm) { meta = loadYaml(fm[1]) ?? {}; text = text.slice(fm[0].length); }

  const preamble = [];
  const sections = [];
  let inFence = false;
  for (const line of text.split('\n')) {
    if (/^\s*(```|~~~)/.test(line)) inFence = !inFence;
    const h = !inFence && line.match(/^## (.+?)\s*$/);
    if (h) { sections.push({ heading: h[1], lines: [] }); continue; }
    (sections.length ? sections[sections.length - 1].lines : preamble).push(line);
  }
  return {
    meta,
    preamble: preamble.join('\n'),
    sections: sections.map((s) => ({ heading: s.heading, body: s.lines.join('\n') })),
  };
}

const DETAILS = /<details>\s*<summary>([\s\S]*?)<\/summary>([\s\S]*?)<\/details>/g;

/** Split a section body into prose and <details> cards, in order. */
export function splitDetails(body) {
  const parts = [];
  let last = 0;
  for (const m of body.matchAll(DETAILS)) {
    if (m.index > last) parts.push({ type: 'prose', text: body.slice(last, m.index) });
    parts.push({ type: 'card', summary: m[1].trim(), body: m[2].trim() });
    last = m.index + m[0].length;
  }
  if (last < body.length) parts.push({ type: 'prose', text: body.slice(last) });
  return parts.filter((p) => p.type === 'card' || p.text.trim());
}

export const cardsOf = (section) => (section ? splitDetails(section.body).filter((p) => p.type === 'card') : []);
export const findSection = (doc, name) => doc.sections.find((s) => s.heading.trim().toLowerCase() === name.toLowerCase());

/** "Hint 2: \"Call to super…\"" → "\"Call to super…\"" (the PDF numbers hints itself). */
export const hintTitle = (summary) => summary.replace(/^Hint\s*\d+\s*[:.–—-]\s*/i, '');

/** Plain text of a Markdown/HTML snippet as it reads once rendered, for comparing with text extracted from a PDF. */
export function plain(s, { inline = false } = {}) {
  const html = inline ? marked.parseInline(s) : marked.parse(s);
  return html
    .replace(/<[^>]+>/g, ' ')
    .replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/&amp;/g, '&')
    .replace(/\s+/g, ' ')
    .trim();
}

/** Letters and digits only, lower-case, ligatures unfolded: robust to PDF spacing, wrapping and quote styles. */
export const squash = (s) => s.normalize('NFKC').toLowerCase().replace(/[^a-z0-9]/g, '');
