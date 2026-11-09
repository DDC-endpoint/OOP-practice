# HANDOFF — OOP-practice

_Update this file at the end of every session. Overwrite the top three sections, and append to the log._

**Last updated:** 2026-11-09

## Current state
- **The pack is fully built.** SpecClaw change `001-lbp-practice-pack` has every task T1–T9 complete. The build is recorded as done, and verify is in progress.
  - **Drills:** 11 (`d01`–`d11`), one aggregator. Every starter fails its tests, and every solution passes.
  - **Papers:** P1, P2 and P3 (the official starters, unchanged) and M1 (the new courier mock). Each has a `PAPER.md`, and each is marked by `marking/Mark.java` (solutions score 100 each; untouched starters score 4, 4, 4 and 0).
  - **Other files:** `papers/*.md` (16 files for the site), the README with a plan to the LBP, and `scripts/selfcheck.sh`. The last full selfcheck run gave 43 PASS and **SELFCHECK OK**.
- **Branches** (all pushed to https://github.com/DDC-endpoint/OOP-practice, private):
  - `specclaw/001-lbp-practice-pack` has the content.
  - `solutions` is the content branch merged in, plus the solution files.
  - `main` has only the baseline until the PR merges.
- **Solution workflow:** solution files are staged in `.staging/solutions/` (gitignored, local only). The lead merges the build branch into `solutions` and overlays the staging files.
- **Decisions recorded in the spec:**
  - The AC-6 scan allows `getClass().getSimpleName()` only for DTO type labels (P1/P2).
  - d11 `timeMinutes` is 25 (stretch).
  - d10 has 3 planted bugs, and 7 of its tests already pass on the starter.
- I deliberately did not run `specclaw-build finalize`. It would merge the branch into `main` locally and delete it, which bypasses verify and the PR. Instead the build was marked done with `specclaw-set-phase … build done`.

## Next steps
1. Finish `/specclaw:verify`, then `/specclaw:pr`, then merge the PR to `main`. After that, `main` holds the content, so update the README's note about switching branches.
2. Keep `solutions` in sync: always run `git switch solutions && git merge main`, never the other way round.
3. The student works through the README plan: P1 timed on Thu 12 Nov, P2 on Fri, M1 on Sat.

## Open questions
- The exact LBP date. It's the Week 9 seminar, around 16–20 Nov 2026, and the qualifying mark is 30.

## Session log
- **2026-11-09:** Design agreed. Created the folder and set up SpecClaw. Wrote the proposal, spec, design and tasks. Built T1 and T2 myself; T3–T8 were built by parallel subagents and integrated. Wrote the README. Selfcheck is green, and verify has started.
