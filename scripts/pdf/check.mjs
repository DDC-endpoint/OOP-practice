// Reads the text back out of every generated PDF and checks it against the Markdown:
//   AC-2  no hint or viva answer leaks into LESSON.pdf
//   AC-3  HINTS.pdf has every hint and every viva Q+A, in source order
//   AC-4  LESSON.pdf has the core sections, the title and every viva question
//   AC-5  PAPER.pdf has every `## ` heading of PAPER.md (and guides/<NAME>.pdf of guides/<NAME>.md)
// Usage: node scripts/pdf/check.mjs        (exit 1 on any failure)
import { existsSync } from 'node:fs';
import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import { getDocument } from 'pdfjs-dist/legacy/build/pdf.mjs';
import { ROOT, sources, parseDoc, cardsOf, findSection, hintTitle, plain, squash } from './lib.mjs';

/** Expected text: render the Markdown, then squash. `inline` for one-line summaries. */
const md = (s) => squash(plain(s));
const mdi = (s) => squash(plain(s, { inline: true }));
const PREFIX = 40;

/** All the text in a PDF, squashed. */
async function pdfText(rel) {
  const data = new Uint8Array(await readFile(join(ROOT, rel)));
  const task = getDocument({ data, verbosity: 0 });
  const pdf = await task.promise;
  let out = '';
  for (let i = 1; i <= pdf.numPages; i++) {
    const page = await pdf.getPage(i);
    out += (await page.getTextContent()).items.map((it) => it.str).join(' ') + '\n';
  }
  await task.destroy();
  return squash(out);
}

let fails = 0, checks = 0;
const fail = (msg) => { fails++; console.log(`FAIL  ${msg}`); };
const expect = (ok, msg) => { checks++; if (!ok) fail(msg); };

/** Every needle appears in the haystack, each after the previous one. */
function inOrder(hay, needles, label) {
  let at = 0;
  for (const n of needles) {
    checks++;
    const i = hay.indexOf(n, at);
    if (i < 0) { fail(`${label}: missing or out of order: "${n.slice(0, 50)}"`); continue; }
    at = i + n.length;
  }
}

const all = sources();
for (const s of all) {
  const doc = parseDoc(s.src);
  const out = (f) => join(s.dir, f);
  const pdfs = s.kind === 'drill' ? ['LESSON.pdf', 'HINTS.pdf'] : [s.kind === 'guide' ? s.out : 'PAPER.pdf'];
  const missing = pdfs.filter((f) => !existsSync(join(ROOT, out(f))));
  if (missing.length) { missing.forEach((f) => fail(`${out(f)} does not exist (run scripts/build-pdfs)`)); continue; }

  if (s.kind === 'paper' || s.kind === 'guide') {
    const text = await pdfText(out(pdfs[0]));
    inOrder(text, doc.sections.map((x) => mdi(x.heading)), out(pdfs[0]));
    continue;
  }

  const hints = cardsOf(findSection(doc, 'Hints'));
  const viva = cardsOf(findSection(doc, 'Viva questions'));
  const lesson = await pdfText(out('LESSON.pdf'));
  const hintPdf = await pdfText(out('HINTS.pdf'));

  // AC-4
  expect(lesson.includes(mdi(String(doc.meta.title ?? ''))), `${out('LESSON.pdf')}: title missing`);
  inOrder(lesson, ['The idea', 'Worked example', 'Your task', 'Viva questions'].map(squash), out('LESSON.pdf'));
  inOrder(lesson, viva.map((q) => mdi(q.summary)), `${out('LESSON.pdf')} viva questions`);
  expect(hints.length > 0, `${s.src}: no hints found under ## Hints`);

  // AC-2: nothing from the hints or the viva answers in the lesson. Text the lesson's own sections
  // already contain (e.g. a method signature the task names) isn't a leak, so it's excluded.
  const own = md([doc.preamble, ...doc.sections.filter((x) => !/^(hints|viva questions)$/i.test(x.heading.trim())).map((x) => x.body)].join('\n\n'));
  const leaks = (needle) => needle.length >= 12 && lesson.includes(needle) && !own.includes(needle);
  for (const [i, h] of hints.entries()) {
    expect(!leaks(mdi(hintTitle(h.summary))), `${out('LESSON.pdf')}: leaks hint ${i + 1} title "${hintTitle(h.summary)}"`);
    expect(!leaks(md(h.body).slice(0, PREFIX)), `${out('LESSON.pdf')}: leaks hint ${i + 1} body`);
  }
  for (const [i, q] of viva.entries()) {
    expect(!leaks(md(q.body).slice(0, PREFIX)), `${out('LESSON.pdf')}: leaks viva answer ${i + 1}`);
  }

  // AC-3: hints then viva Q+A, all present, in source order
  inOrder(hintPdf, [
    ...hints.flatMap((h) => [mdi(hintTitle(h.summary)), md(h.body).slice(0, PREFIX)]),
    squash('Viva answers'),
    ...viva.flatMap((q) => [mdi(q.summary), md(q.body).slice(0, PREFIX)]),
  ].filter(Boolean), out('HINTS.pdf'));
}

const drills = all.filter((s) => s.kind === 'drill').length;
const guides = all.filter((s) => s.kind === 'guide').length;
const papers = all.length - drills - guides;
console.log(`${fails ? 'FAIL' : 'PASS'}  ${checks - fails}/${checks} checks · ${drills} drills (LESSON + HINTS) · ${papers} papers · ${guides} guides`);
process.exit(fails ? 1 : 0);
