# Tasks: Align d10 and d11 with the official LBP exam-day setup

**Change:** 003-d10-d11-exam-day-swagger
**Created:** 2026-11-19
**Total Tasks:** 4

## Summary

Add springdoc 2.6.0 to both drills, rewrite the run/check/submit prose for Swagger, Section C and the 110+10 split, regenerate the PDFs and paper copies, then merge into `solutions` and test.

## Tasks

### Wave 1: content

- [x] `T1` — Branch from `main`, carrying the working tree over, and add springdoc 2.6.0 to the d10 and d11 poms
  - Files: drills/d10-debug-and-check/pom.xml, drills/d11-stateful-commit/pom.xml
  - Estimate: small
  - Kind: config
  - Notes: the d10 pom must equal the student's working-tree content. Run the d10 and d11 tests on `main` code: d10 fails only on the 3 planted bugs, and d11 starts its Spring context (the TODO tests fail as expected) (AC-1).
- [x] `T2` — Rewrite d10 `LESSON.md` (FR-2..5, FR-7)
  - Files: drills/d10-debug-and-check/LESSON.md
  - Estimate: medium
  - Kind: docs
- [x] `T3` — Rewrite d11 `LESSON.md` (FR-6, FR-7)
  - Files: drills/d11-stateful-commit/LESSON.md
  - Estimate: small
  - Kind: docs

### Wave 2: generate, commit, solutions

- [x] `T4` — Regenerate the PDFs and paper copies, commit the file map only, merge into `solutions`, then run the tests and the Swagger smoke check there
  - Files: drills/d10-debug-and-check/{LESSON,HINTS}.pdf, drills/d11-stateful-commit/{LESSON,HINTS}.pdf, papers/d10.md, papers/d11.md
  - Estimate: medium
  - Kind: config
  - Depends: T1, T2, T3
  - Notes: AC-2, AC-3, AC-6, AC-7. Switching to `solutions` with the student's dirty tree may conflict on `src/main`. If so, use a temporary `git worktree` for `solutions` instead of touching their tree.

---

## Legend

- `[ ]` Pending
- `[~]` In Progress
- `[x]` Complete
- `[!]` Failed
- `[>]` Deferred
