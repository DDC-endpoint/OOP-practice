# HANDOFF — OOP-practice

_Update this file at the end of every session. Overwrite the top three sections, and append to the log._

**Last updated:** 2026-11-20

## Current state
- **All work is merged to `main` and archived.** Four SpecClaw changes, each verified PASS, are in `.specclaw/changes/archive/`:

  | Change | PR | What it did |
  |---|---|---|
  | `001-lbp-practice-pack` | #1 | the pack itself; tagged `lbp-pack-v1` |
  | `002-drill-paper-pdfs` | #2 | `LESSON.pdf` / `HINTS.pdf` / `PAPER.pdf` |
  | `003-d10-d11-exam-day-swagger` | #3 | Swagger in d10 and d11, Section C wording, the 110+10 ZIP split |
  | `004-m1-exam-conventions-and-guides` | #4 | M1 reworked to the module mock's conventions and the official format; `guides/EXAM-DAY.pdf`, `guides/REFRESHER.pdf`; README fork steps for friends |

- **Contents:**
  - 11 drills
  - P1–P3 (the original 100-minute A/B format) and M1 (the official 120-minute Q1/Q2/Section C format)
  - scored marking: solutions score 100, untouched starters ≤ 4 (M1: 0)
  - `papers/*.md`
  - two guides
- **The exam facts behind 003 and 004:**
  - From the official instructions (saved in `D:\year-2-prep\OOP\05_LBP_Practice_Papers\OOP_LBP_00_Exam_Day_Instructions.md`): 120 min, JDK 25 + IntelliJ Ultimate, Swagger in the starter, exactly one new class.
  - From the module's own mock: plain classes not records, `GlobalExceptionHandler` / `ErrorResponse`, no `/api` prefix, Spring Boot 4.1.1.
- **Branches.** `main` and `solutions` are in sync and pushed (`solutions` differs only under `src/main`).
- **The repo is PUBLIC.** Friends fork it (README → "Friends: fork it and sit M1"). `solutions` is visible to them, and the user accepted that.
- **The student's working tree** (`D:\OOP-practice`, on `main`) holds their own uncommitted attempts: d01–d09 `src/main`, an M1 attempt that scored 98/100, and staged `Annual.java` files. Never commit those. Use worktrees.
- Project rules and gotchas are in `.specclaw/context.md`.

## Next steps
1. The LBP is on Fri 20 Nov 2026. Afterwards, ask how it went and whether anything in the real paper should become a drill or an M2.
2. Optional: port P1–P3 to the module-mock conventions (no records, `/resource` paths, Swagger) as one SpecClaw change, if friends keep using the pack.
3. Optional: reword the comment "record skipped IDs" in M1 `ParcelService` (a cosmetic grep false positive).

## Open questions
- Do the friends want P1–P3 in the new conventions too?

## Session log
- **2026-11-09:** Design agreed. Created the folder and set up SpecClaw. Wrote the proposal, spec, design and tasks. Built T1 and T2 myself; T3–T8 were built by parallel subagents and integrated. Wrote the README. Selfcheck is green, and verify has started.
- **2026-11-09 (later):** Verify PASS, PR #1 merged, change archived, tagged `lbp-pack-v1`.
- **2026-11-12:** Change 002 (PDFs), PR #2.
- **2026-11-19/20 (the night before the LBP):**
  - Change 003 (d10/d11 Swagger and the exam-day format) and change 004 (M1 rework + guides). Verified in clean worktrees and merged as PRs #3 and #4. `solutions` synced and pushed.
  - Archived 002–004. Updated `context.md`, `CLAUDE.md` and this file.
  - The student sat the reworked M1 and scored 98/100. The lost 2 marks were ExpressParcel's unchecked `+` after `multiplyExact`.
