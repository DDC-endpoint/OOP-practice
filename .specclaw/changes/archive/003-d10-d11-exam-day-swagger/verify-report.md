# Verify report: 003-d10-d11-exam-day-swagger

**Verdict:** PASS

Checked 2026-11-20 against a clean worktree at the change branch head `debc930`. The evidence comes from `specclaw-verify collect` (run in that worktree), from `reports/T4.md`, and from a Swagger smoke run via `mvnw spring-boot:run`. E2E is not configured, so there is **no e2e evidence**.

## Acceptance criteria

| AC | Result | Evidence |
|---|---|---|
| AC-1 | PASS | Both poms add `springdoc-openapi-starter-webmvc-ui` `2.6.0`. selfcheck: `drills/d10-debug-and-check starter fails 3/10 tests (unsolved, as intended)`, which are the 3 planted bugs. There is no context failure, and no `.java` file is in the diff. |
| AC-2 | PASS | selfcheck `every drill test passes on solutions` (`reports/T4.md`: SELFCHECK OK, 53 PASS, when `solutions` held only 003). |
| AC-3 | PASS | d10 `/swagger-ui/index.html` → 200. Its api-docs list `/api/screenings`, `/api/screenings/quotes`, `/api/screenings/{id}` and `/api/screenings/{id}/quote`. d11 → 200, listing `/api/rooms/bookings`, `/api/rooms/commit` and `/api/rooms/preview`. |
| AC-4 | PASS | The d10 `LESSON.md` contains "Section C" (5), "Swagger" (17), "Try it out" (4), "Execute" (5), "110" (7) and "Blackboard" (6). curl appears only in the fallback paragraph (lines 79–88) and as a parenthetical in the 415 row (line 144). |
| AC-5 | PASS | The d11 `LESSON.md:201–217` is a Swagger walkthrough: Try it out, an identical retry, 6 bookings, and a 409 CONFLICT step. curl is a fallback at line 217. |
| AC-6 | PASS | The PDF leak check gave PASS 543/543 (`reports/T4.md`). The d10/d11 PDFs and `papers/d10.md`, `papers/d11.md` are all in the diff, and selfcheck AC-8 lesson sections and front matter are ok. |
| AC-7 | PASS | This was met when `solutions` held only 003 (53 PASS). See the stacking note. |
| AC-8 | PASS | `git diff --name-only origin/main...HEAD` gives exactly the 10 files in the file map, with no `.java` changes (NFR-1). |

## Selfcheck stacking note

A later selfcheck from 003's checkout reported `SELFCHECK FAILED (2)`: `AC-3 m1 solution scores 0/100` and `AC-10 solutions also changes: …`. By then, local `solutions` had also merged the stacked change `004-m1-exam-conventions-and-guides` (`d28708e`).

The verifier confirmed:
- every file in the AC-10 list belongs to 004's diff
- none of 003's files are in that list
- the m1 failure comes from 004's reworked M1 solution being scored by 003's older marking

**Decision:** AC-7 is met for this change. selfcheck must be re-run from 004's branch, which includes 003, before merging. It is recorded in 004's verify.

## Findings

1. E2E is not configured, so there is no e2e evidence. Swagger is covered by the manual smoke run only. Informational.
2. The latest selfcheck from 003's own checkout isn't clean, because of the stacking. Re-run it on 004, then merge both.
3. AC-1 and AC-2 rest on selfcheck counts and `reports/T4.md`. The tests were not re-run by the verifier. Low risk.
4. Scope is clean: only the 10 mapped files changed, and no `.java`.

**Passed:** 8/8. **Failed:** 0/8. **Verdict:** PASS
