# Proposal: PDF versions of the drill lessons, hints and papers

**Created:** 2026-11-12
**Status:** 🟡 Draft

## Problem

The drills and papers are only Markdown. They read well in IntelliJ, but not on a phone or tablet, or when printed. The hints and viva answers sit in `<details>` blocks inside `LESSON.md`. A static document can't fold those away, so a straight conversion would show the hints and answers right under the task. The student wants clean, well-spaced PDFs to read alongside the exercise. The LBP is on 20 Nov 2026, so this needs to land quickly.

## Proposed Solution

Add a rebuild script that renders Markdown → styled HTML → PDF using the headless Chrome or Edge already on the PC (there's no pandoc).

- **For each drill d01–d11**, write these next to `LESSON.md`, which stays as it is:
  - `LESSON.pdf`: front-matter header (title, difficulty, time, tasks and marks), then The idea, Worked example, Your task, and Viva questions as **questions only**.
  - `HINTS.pdf`: the hints numbered in order, each shown as a card with its summary as the title, then the viva questions **with answers**.
- **For each paper** (P1–P3 in `past-papers/`, M1 in `mock-papers/`), write `PAPER.pdf` next to `PAPER.md`.
- **Style** (screen-first): A4, generous spacing and line height, a readable sans-serif body, dark code panels with syntax colouring, a clear heading hierarchy, page numbers, and no page breaks inside code blocks or hint cards.
- **Rebuild:** add `scripts/build-pdfs` (one command, run from anywhere). Document it in `README.md` next to `sync-papers.sh`.
- Commit the PDFs and open a PR, so they're available on every device after `git pull`.

## Scope

### In Scope
- 11 × `LESSON.pdf` and 11 × `HINTS.pdf`, plus 4 × `PAPER.pdf`.
- A rebuild script, plus any rendering dependencies it needs (pinned, dev-only).
- A README section: "Reading on paper / tablet".

### Out of Scope
- The paper `README.md` files, the root `README.md`, and `papers/*.md` (copies for the study site).
- Any change to the content of `LESSON.md` or `PAPER.md`.
- The `solutions` branch.
- Auto-regenerating the PDFs in CI or a git hook. The script is run by hand.

## Impact

- **Size:** bounded. It adds a render script beside the existing `scripts/sync-papers.sh` and changes no existing flow, Maven module or marking code.
- **Files affected:** about 30. That's 26 generated PDFs, plus the script, its stylesheet and any lockfile, plus README and `.gitignore`.
- **Complexity:** small to medium. The main work is splitting the `<details>` blocks correctly and getting the print CSS right.
- **Risk:** low. The PDFs are only new outputs. The one risk is a hint or answer leaking into `LESSON.pdf`, and verify should check for that.

## Open Questions

- Which renderer? There are two options:
  - Node with `marked` and `highlight.js`, plus `puppeteer-core` driving the installed Chrome. This gives page-number footers.
  - A dependency-free Chrome `--print-to-pdf`. This gives no custom footers.
  - **Default:** the Node option, with devDependencies kept under `scripts/pdf/`.
- Do the papers have any `<details>` content that should be held back? Assume none. `PAPER.md` is rendered whole.
