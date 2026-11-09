# Verify Report — 001-lbp-practice-pack

**Verdict: PASS**

All acceptance criteria AC-1..AC-10 met. E2E: **not configured — no e2e evidence** (`e2e_state = not_configured`); not counted as a pass. Fast tier `bash scripts/selfcheck.sh` → `SELFCHECK OK` (`tests_passed: true`).

## Acceptance Criteria

| AC | Result | Evidence |
|---|---|---|
| AC-1 | PASS | Selfcheck: drills compile; P1/P2/P3/M1 compile. Every drill fails untouched: d01 8/8, d02 7/7, d03 24/24, d04 8/8, d05 8/8, d06 8/8, d07 9/9, d08 6/6, d09 10/10, d10 3/10 (debug drill, by design), d11 20/20. |
| AC-2 | PASS | "every drill test passes on solutions". |
| AC-3 | PASS | On `solutions` M1/P1/P2/P3 = 100/100; untouched starters M1 0, P1 4, P2 4, P3 4 (≤ 20). |
| AC-4 | PASS | `marking/Mark.java` aborts with `MARKING ERROR` (exit 2) on mismatched sums; 100/100 results prove 18/22/20/20/20. |
| AC-5 | PASS (spot-checked) | d03 (8 ceiling cases incl. MAX_VALUE, 7 tier cases, overflow), d05 (scrambled 8-team league + per-tie-break pairs), M1 Task3 (stub parcels, several ids). |
| AC-6 | PASS | d04 test 8 strips comments and scans for `instanceof`/`getClass(`; `SourceScan.assertNoTypeChecks` in M1/P1/P2 Task2Test. Recorded refinement: `getClass().getSimpleName()` allowed as a label only; P3's paper does not forbid instanceof. |
| AC-7 | PASS | `Reflect.java` in every drill testkit and marking suite; tests compile and run against untouched starters. |
| AC-8 | PASS | 11 LESSON.md pass section check (8–11 details blocks); 15 papers/*.md front matters pass. |
| AC-9 | PASS | `gh repo view DDC-endpoint/OOP-practice` → `PRIVATE`; `main`, `solutions`, `specclaw/001-lbp-practice-pack` pushed and matching local. |
| AC-10 | PASS | Name-only diff feature-branch vs `solutions` excluding `src/main` is empty; selfcheck "AC-10 solutions differs only in src/main". |

## FRs / NFRs
FR-1..FR-10 met (runner + wrappers, `_mN` marks, Section B tests per paper, solutions branch, papers front matter, README, `build.test_command`). NFR-1/2 consistent with wrapper builds passing everywhere; NFR-3..5 judged from lessons read (plain, code-first, fictional). NFR-4 timing not measured.

## Recorded deviations (accepted)
- AC-6 refinement (T7): the scan allows `getSimpleName()` labels anywhere in the scanned file — slightly broader than "only in a given DTO"; low risk since string-literal class names are also banned.
- d11 `timeMinutes: 25` (stretch drill) vs NFR-4's 10–20 guidance.

## Caveats
- The pack reaches `main` only when the PR merges; until then students use `specclaw/001-lbp-practice-pack` (README says so).
- AC-5..AC-7 rest on a spot-check (verifier read d03, d05, d04 scan and four marking suites); Maven not re-run by the verifier (selfcheck did run it).

_Verifier: SpecClaw verify agent (sonnet), 2026-11-09. Code review step skipped (`workflow.code_review: false`)._
