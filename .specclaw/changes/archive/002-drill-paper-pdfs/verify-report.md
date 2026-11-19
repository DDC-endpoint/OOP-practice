# Verification Report: 002-drill-paper-pdfs

**Verified:** 2026-11-12
**Model:** Sonnet (independent verify agent)
**Verdict:** PASS

## Acceptance Criteria

- ✅ **AC-1:** `bash scripts/build-pdfs.sh` exited 0 and printed `Built 26 PDFs in 5.8s.` `find drills past-papers mock-papers -name '*.pdf' | wc -l` returned 26.
- ✅ **AC-2:** `check.mjs` passed 543/543. That covers no hint title, hint body prefix or viva answer prefix in any LESSON.pdf. The verifier also searched the extracted text of all 11 LESSON.pdf files for `\bHints\b` and found no match. A mutation test during the build (HINTS.pdf copied over LESSON.pdf) failed with 12 leaks.
- ✅ **AC-3:** The `inOrder` check over the hint titles and bodies, then "Viva answers", then each viva Q and A, passed for all 11 drills.
- ✅ **AC-4:** The title, the four core headings in order, and every viva question are present.
- ✅ **AC-5:** Every `## ` heading of P1, P2, P3 and M1 is present in order.
- ✅ **AC-6:** Human-reviewed. The user approved the look on 2026-11-12 ("I like the look. commit and push"), after viewing the d01 pair. The implementer also read back d10 LESSON and P1 PAPER (see reports/T1.md and T4.md).
- ✅ **AC-7:** `git diff main -- '*.md' ':!README.md' ':!.specclaw/**'` and `git diff main --stat -- '*/src/**' marking/ pom.xml` both printed nothing.
- ✅ **AC-8:** `scripts/pdf/node_modules` is ignored. `package-lock.json` is tracked.
- ✅ **AC-9:** A rebuild changes only `*.pdf` files, and the check still passes. The PDFs aren't byte-identical across rebuilds because of the embedded timestamps.

## Constraints and FRs

- FR-1, FR-5, FR-6 and NFR-3 are met. The rebuild takes about 6–8 s.
- `context.md`: `solutions` was merged from the branch, and `git diff --stat solutions specclaw/002-drill-paper-pdfs -- . ':!*/src/main/**'` is empty.

## Test Results

- `bash scripts/selfcheck.sh` (configured `test_command`) passed. It covers the AC-1, AC-2, AC-3, AC-8 and AC-10 pack checks: every starter fails as intended, the solutions score 100/100, and the untouched starters score ≤ 20.
- `node scripts/pdf/check.mjs`: `PASS  543/543 checks · 11 drills (LESSON + HINTS) · 4 papers`.

## Issues Found (non-blocking)

1. `check.mjs` has no explicit "Hints heading absent" assertion. The verifier's independent search covers it.
2. The hint count isn't cross-checked against `<details>` with an independent parser.

## Summary

**Passed:** 9/9. **Failed:** 0/9. **Verdict:** PASS
