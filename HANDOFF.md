# HANDOFF — OOP-practice

_Update this file at the end of every session. Overwrite the top three sections, and append to the log._

**Last updated:** 2026-11-09

## Current state
- SpecClaw change `001-lbp-practice-pack` is **in build** (proposal, spec, design and tasks are approved and done; the review panel was skipped by choice). The work is on branch `specclaw/001-lbp-practice-pack`, and `main` only has the baseline until the PR merges.
- Remote: **https://github.com/DDC-endpoint/OOP-practice** (private). The `gh` token's real login is `DDC-endpoint`, even though the keyring labels it EnderGuardian25.
- Done:
  - T1: scaffold, Maven 3.9.9 wrapper, `marking/Mark.java` (tested), `scripts/selfcheck.sh`, `scripts/sync-papers.sh`, `templates/Reflect.java`.
  - T2: drills d01–d03 (starters fail every test, and solutions on `solutions` are green), starter README. Pushed.
- In flight (parallel subagents): T3 d04–d06, T4 d07–d09, T7 P1–P3 projects + marking + solutions. The order was changed so P1–P3 come before T5/T6/T8, because they matter most for the exam.
- How solutions flow: agents put solution files in `.staging/solutions/<path>` (gitignored). The lead commits starters on the build branch, then `git switch solutions && git merge <build branch>`, overlays the staging files, runs the tests green, commits, and switches back.
- Lesson front matter lives in `LESSON.md` / `PAPER.md`. `scripts/sync-papers.sh` copies it to `papers/<id>.md`.
- Added from the friend's hub: every lesson ends with `## Viva questions` (spec FR-1 updated).

## Next steps
1. Integrate T3/T4/T7 (register modules in the root pom, sync papers, commit, solutions branch, selfcheck, push).
2. T5 d10, T6 M1, T8 d11 (can run in parallel), then T9: full README with a day-by-day schedule, selfcheck green, push.
3. `/specclaw:verify` → `/specclaw:pr` → merge to `main`.

## Open questions
- Exact LBP date (Week 9 seminar, about 16–20 Nov 2026). The qualifying mark is 30.

## Session log
- **2026-11-09:** Design agreed, folder created, SpecClaw initialised, proposal drafted.
