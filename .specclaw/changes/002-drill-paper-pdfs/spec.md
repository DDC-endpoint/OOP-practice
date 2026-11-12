# Spec: PDF versions of the drill lessons, hints and papers

**Change:** 002-drill-paper-pdfs
**Created:** 2026-11-12
**Status:** 🟡 Draft

## Overview

Add a one-command rebuild that renders each drill's `LESSON.md` into two PDFs:
- `LESSON.pdf` holds the lesson. The hints and viva answers are held back.
- `HINTS.pdf` holds the hints, then the viva answers.

It also renders each `PAPER.md` into `PAPER.pdf`. The PDFs are written next to their source file and committed. The source Markdown is never modified.

## Approach

- **Renderer:** Node ESM script `scripts/pdf/build.mjs`. It uses:
  - `js-yaml` to read the front matter
  - `marked` to render the Markdown
  - `highlight.js` for code colouring
  - `puppeteer-core` to drive the installed Chrome or Edge, `page.pdf()` on A4 with a page-number footer
  - `style.css`, inlined into each page
- **Splitting:** cut the Markdown body at `## ` headings, then turn each `<details><summary>S</summary>B</details>` into a card before rendering:
  - **LESSON:** every section except `## Hints`, with the viva cards reduced to numbered questions.
  - **HINTS:** the `## Hints` cards numbered in order, then the viva cards with their answers.
- **Checker:** `scripts/pdf/check.mjs` (using `pdfjs-dist`) extracts the PDF text and asserts AC-2 to AC-5.
- **Files:**
  - New: `scripts/pdf/{package.json, package-lock.json, build.mjs, check.mjs, style.css}`, `scripts/build-pdfs.sh`, `scripts/build-pdfs.cmd`, 26 generated PDFs.
  - Modified: `README.md`, `.gitignore` (adds `scripts/pdf/node_modules/`).

## Requirements

### Functional Requirements

- **FR-1:** `scripts/build-pdfs.sh` (Git Bash) and `scripts\build-pdfs.cmd` (cmd/PowerShell) work from any working directory.
  - Run `npm ci` in `scripts/pdf` when `node_modules` is missing, then run `build.mjs`.
  - The build writes all 26 PDFs.
- **FR-2:** For each `drills/dNN-*/LESSON.md`, write `LESSON.pdf` in the same folder, containing:
  - a title block from the front matter: id and title, difficulty, time in minutes, and a task table of names and marks
  - The idea, Worked example and Your task, in full
  - Viva questions as a numbered list of the question text only
  - any other `## ` section except Hints, in source order
- **FR-3:** For each drill, write `HINTS.pdf` in the same folder, containing:
  - a title block naming the drill
  - a **Hints** part with one card per hint, in source order, numbered 1..N. The card title is the `<summary>` text and the body is the rendered Markdown.
  - a **Viva answers** part with one card per viva question. The question is the card title and the answer is the body.
- **FR-4:** For each `past-papers/*/PAPER.md` and `mock-papers/*/PAPER.md`, write `PAPER.pdf` in the same folder. It holds:
  - a title block from the front matter, with the task and marks table
  - the whole body
- **FR-5:** Choosing the browser:
  - use `CHROME_PATH` if it is set
  - otherwise try Chrome, then Edge, at their standard Windows install paths
  - if none is found, exit non-zero with a message saying to set `CHROME_PATH`
- **FR-6:** `README.md` gains a short "Reading on paper / tablet" section that names the PDFs and the rebuild command.

### Non-Functional Requirements

- **NFR-1 (style):** A4, designed for screens first.
  - Body: 11–12pt sans-serif, line height ≥ 1.5, clear spacing between headings.
  - Code: dark panels with syntax colouring, monospace ≥ 9.5pt, long lines wrapped and never cut off.
  - Hint and viva answer cards: visibly boxed.
  - Footer: "dNN · Lesson|Hints · page X of Y".
  - No page break inside a code block, card or table row where avoidable.
- **NFR-2:** Dependencies are pinned in `package-lock.json` and live only under `scripts/pdf/`. Nothing is added to the Maven build, and no browser is downloaded, because `puppeteer-core` uses the installed one.
- **NFR-3:** Rebuilding everything takes under 60 s on this PC.

## Acceptance Criteria

- **AC-1:** One run of `bash scripts/build-pdfs.sh` exits 0 and leaves 26 PDFs:
  - 11 × `drills/*/LESSON.pdf`
  - 11 × `drills/*/HINTS.pdf`
  - 4 × `{past,mock}-papers/*/PAPER.pdf`
- **AC-2 (no spoilers):** No `LESSON.pdf` contains:
  - the "Hints" heading
  - any hint card title
  - the first 40 characters of any hint body or viva answer, after whitespace is normalised

  `check.mjs` asserts this over all 11 drills.
- **AC-3:** Every `HINTS.pdf` contains every hint summary and viva question from its `LESSON.md`, in source order, and the first 40 characters of each answer body. The hint count matches the number of `<details>` in `## Hints`.
- **AC-4:** Every `LESSON.pdf` contains:
  - the headings The idea, Worked example, Your task and Viva questions
  - every viva question's text
  - the drill title from the front matter
- **AC-5:** Every `PAPER.pdf` contains every `## ` heading of its `PAPER.md`.
- **AC-6 (visual, human-reviewed):** Page-1 PNGs of `d01/LESSON.pdf`, `d01/HINTS.pdf`, a code-heavy page of `d10/LESSON.pdf`, and `P1/PAPER.pdf` are captured and checked. They should show:
  - dark syntax-coloured code
  - boxed cards
  - a page-number footer
  - readable spacing with nothing cut off

  The user approves the look.
- **AC-7:** `git diff main -- '*.md' ':!README.md' ':!.specclaw/**'` is empty, meaning no source Markdown changed. `git diff main --stat -- '*/src/**' marking/ pom.xml` is also empty.
- **AC-8:** `scripts/pdf/node_modules/` is gitignored, and `package-lock.json` is committed.
- **AC-9:** Re-running the build is idempotent. It changes no file except the PDFs it rewrites, and `check.mjs` still passes.

## Edge Cases

- A `<summary>` can contain inline HTML or code, such as `<code>super(...)</code>`. It must render as code, not literal tags.
- A hint body can contain fenced code. It needs a blank line after `<summary>` before it renders as Markdown.
- d05 and d07 have different numbers of hints and viva questions (5/4 and 4/5). Count each section on its own.
- `PAPER.md` has no `<details>`, but it does have Markdown tables and code. Tables must not overflow A4.
- A Windows path with spaces (`C:\Program Files\...`) must reach the browser launch correctly.

## Dependencies

- Node ≥ 18 (v24.15 is installed) and npm.
- Chrome 157 or Edge 157 is installed.
- Pinned npm packages: `marked`, `highlight.js`, `js-yaml`, `puppeteer-core`, `pdfjs-dist`.

## Notes

- **Solutions branch (`context.md` constraint):** after any commit on the content branch, run `git switch solutions && git merge <branch>`. The PDFs aren't under `src/main`, so AC-10 holds. Do the merge when the PR lands.
- `papers/*.md` (the study-site copies) are out of scope and not regenerated.
- The panel was skipped by the user on 2026-11-12. The renderer was settled as Node with puppeteer-core, as the default from the proposal.
