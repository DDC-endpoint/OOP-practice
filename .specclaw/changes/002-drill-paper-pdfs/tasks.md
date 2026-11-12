# Tasks: PDF versions of the drill lessons, hints and papers

**Change:** 002-drill-paper-pdfs
**Created:** 2026-11-12
**Total Tasks:** 4

## Summary

- **Wave 1:** build the renderer and its wrappers.
- **Wave 2:** the checker and the generated PDFs. Then a visual review with the user before the commit.

## Tasks

### Wave 1 — renderer

- [x] `T1` — Node renderer: package, build script, stylesheet
  - Files: `scripts/pdf/package.json`, `scripts/pdf/package-lock.json`, `scripts/pdf/build.mjs`, `scripts/pdf/style.css`, `.gitignore`
  - Estimate: medium
  - Kind: impl
  - Notes:
    - Covers FR-2 to FR-5 and NFR-1/2.
    - Split the sections at `## ` and turn `<details>` into cards **before** `marked`.
    - The summary is inline Markdown or HTML.
    - Use the footer `displayHeaderFooter` template for page X of Y.
    - Add `scripts/pdf/node_modules/` to `.gitignore`.
- [x] `T2` — Wrappers + README section
  - Files: `scripts/build-pdfs.sh`, `scripts/build-pdfs.cmd`, `README.md`
  - Estimate: small
  - Kind: docs
  - Depends: T1
  - Notes:
    - Covers FR-1 and FR-6.
    - The wrappers `cd` to the repo root, run `npm ci` if `node_modules` is missing, run the build, then run the check.

### Wave 2 — check, generate, review

- [x] `T3` — Checker
  - Files: `scripts/pdf/check.mjs`
  - Estimate: small
  - Kind: test
  - Depends: T1
  - Notes:
    - Covers AC-2 to AC-5.
    - Re-parse each `LESSON.md` and `PAPER.md` with the same splitter as `build.mjs`. Share it through an exported module.
    - Extract the PDF text with `pdfjs-dist`, then normalise the whitespace before comparing.
- [x] `T4` — Generate the 26 PDFs, then the visual review
  - Files: `drills/*/LESSON.pdf`, `drills/*/HINTS.pdf`, `past-papers/*/PAPER.pdf`, `mock-papers/M1-courier/PAPER.pdf`
  - Estimate: small
  - Kind: config
  - Depends: T2, T3
  - Notes:
    - Run `bash scripts/build-pdfs.sh` until it's green.
    - Screenshot the AC-6 pages to the scratchpad and show them to the user. Change the style until the user approves.
    - Then commit. After the PR merges, merge into `solutions` (`context.md` constraint).

---

## Legend

- `[ ]` Pending
- `[~]` In Progress
- `[x]` Complete
- `[!]` Failed
- `[>]` Deferred
