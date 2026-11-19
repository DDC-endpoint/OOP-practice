# Verify report: 004-m1-exam-conventions-and-guides

**Verdict:** PASS

Checked 2026-11-20 in a clean worktree at the change branch head, diffed against change 003's head (004 is stacked on 003, which is verified PASS and merged as PR #3). E2E is not configured, so there is **no e2e evidence**. The runtime evidence is the selfcheck and a Swagger smoke on the solved M1.

## Acceptance criteria

| AC | Result | Evidence |
|---|---|---|
| AC-1 | PASS | selfcheck from the clean worktree: `m1 solution scores 100/100`, `m1 untouched starter scores 0/100 (≤ 20)`, p1–p3 solutions 100 with starters at 4, `solutions differs only in src/main`, `SELFCHECK OK`. |
| AC-2 | PASS | No `package …courier.dto/.error/.repo`, and no Java records under `mock-papers/M1-courier/src`. The only literal `record ` is the prose comment at `ParcelService.java:19` ("record skipped IDs…"). |
| AC-3 | PASS | `server.port=8080`, springdoc 2.6.0. Smoke (port 18084): `/swagger-ui.html` 200, api-docs `/parcels`, `/parcels/standard`, `/parcels/van`, `/parcels/{id}/quote`; the `NewStandardParcel` schema has all 4 fields; POST → 201; `/parcels/5/quote` → 900 p; `/parcels/999/quote` → `NOT_FOUND`. |
| AC-4 | PASS | `PAPER.md` contains 120, 110, Section C, Swagger, "ONE new" and Blackboard. `/api/parcels` and `8084` are absent from the paper, README, requests.http, `papers/m1.md` and `marking/m1`. |
| AC-5 | PASS | The PDF build gave `PASS 571/571 checks · 11 drills · 4 papers · 2 guides`. Re-run after the refresher fix: PASS 571/571. |
| AC-6 | PASS | The diff against 003's head is within the design file map. `scripts/build-pdfs.{sh,cmd}` (header comments only) are a ripple of the new `guide` kind. |

## Functional requirements

- FR-1 to FR-7 are met. The rules, formulas, seeds, illustrative values and marks in `PAPER.md` are unchanged compared with `origin/main`.
- `Mark.java` swaps `SectionBTest` for `SectionCTest` for m1 only.
- The exam facts in `guides/EXAM-DAY.md` match the official instructions, and the quotes are verbatim.
- The `REFRESHER.md` snippets compile-check by reading.

## Issues and resolution

1. **I-2:** refresher §4 showed a duplicate as `IllegalArgumentException` → 400, which conflicts with M1's 409. **Fixed** in `9e8dd80`: it now uses `DuplicateParcelException`, with a note that duplicates follow the paper (M1 gives 409, the module mock 400).
2. **I-4:** spec FR-1 listed `ErrorResponse` under `model`, but it lives in `exception`. **Spec text corrected.**
3. **I-1 (not fixed, cosmetic):** the comment "record skipped IDs" trips a literal grep. There's no Java record.
4. **I-3 / I-5 (notes):** the build-pdfs header-comment ripple. The smoke ran on 18084, while the configured port is 8080.

**Code review:** not run (`workflow.code_review: false`).

**Passed:** 6/6. **Failed:** 0/6. **Verdict:** PASS
