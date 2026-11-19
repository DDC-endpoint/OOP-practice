# Tasks: Rework M1 to the exam's format and conventions; add guides

**Change:** 004-m1-exam-conventions-and-guides
**Created:** 2026-11-19
**Total Tasks:** 5

## Tasks

### Wave 1 — parallel content

- [x] `T1` — M1 starter: packages, plain classes, `/parcels`, port 8080, springdoc
  - Files: mock-papers/M1-courier/{pom.xml, src/**}
  - Estimate: medium
  - Kind: refactor
- [x] `T2` — M1 paper, README, requests.http to the exam format and conventions
  - Files: mock-papers/M1-courier/{PAPER.md, README.md, requests.http}
  - Estimate: medium
  - Kind: docs
- [x] `T3` — guides/EXAM-DAY.md + guides/REFRESHER.md, plus the PDF pipeline `guide` kind and the README links
  - Files: guides/*.md, scripts/pdf/{lib,build,check}.mjs, README.md
  - Estimate: medium
  - Kind: docs

### Wave 2 — marking, solutions, outputs

- [x] `T4` — marking/m1 + Mark.java to the new names and URLs (SectionCTest)
  - Files: marking/m1/**, marking/Mark.java, scripts/selfcheck.sh (if needed)
  - Estimate: medium
  - Kind: test
  - Depends: T1
- [x] `T5` — Rebuild the PDFs and papers/m1.md, port the solutions, run selfcheck and the Swagger smoke check
  - Files: mock-papers/M1-courier/PAPER.pdf, guides/*.pdf, papers/m1.md
  - Estimate: medium
  - Kind: config
  - Depends: T1, T2, T3, T4

---

## Legend
- `[ ]` Pending · `[~]` In Progress · `[x]` Complete · `[!]` Failed · `[>]` Deferred
