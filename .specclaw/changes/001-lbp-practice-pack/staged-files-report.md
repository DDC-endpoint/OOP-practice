# Staged-files report: 001-lbp-practice-pack

**Verdict:** CHANGES_REQUESTED

## Findings

.specclaw/changes/001-lbp-practice-pack/proposal.md: 🛑 BLOCK: flagged required-missing by rule 5, but already satisfied. It is byte-identical on `main` (31a2b40) and on the branch HEAD (blob 42b105d), so it is absent only from the diff. Nothing needs committing. If the gate must go green, the fix belongs in the gate (see Summary).
.specclaw/changes/001-lbp-practice-pack/design.md: 🛑 BLOCK: same as proposal.md. Identical on `main` and on HEAD (blob 3ca28eb). It is already in the branch history through the baseline commit.
mock-papers/M1-courier/**: ⚠️ WARN: all 17 paths are declared by T5 (`mock-papers/M1-courier/**`) and by the design's File Changes Map. The checker failed to match the glob. This is a parsing false positive, not scope creep.
past-papers/P3-scheduler/**: ⚠️ WARN: all 17 paths are declared by T6 (`past-papers/P3-scheduler/**`) and by the File Changes Map. Same checker glob miss as M1. P1 and P2 matched on the same line, so the likely cause is the parenthetical text after the P3 glob. No action on the files.
scripts/sync-papers.sh: ⚠️ WARN: undeclared, but it is a ripple. It generates `papers/*.md` from `LESSON.md` and `PAPER.md`, and `papers/*.md` is a declared deliverable (File Changes Map, "15 front-matter briefs"). It sits next to the declared `scripts/selfcheck.sh`. No action needed.
templates/Reflect.java: ⚠️ WARN: declared in T1's `Files:` line (`templates/Reflect.java`, source copied into each project) and required by AC-7. The checker did not match it, likely because of the trailing parenthetical. This is not creep.
.gitattributes: ⚠️ WARN: undeclared ripple. Commit e6e74a3 ("line endings (cmd=CRLF, sh/mvnw=LF)") supports the declared `mvnw`, `mvnw.cmd`, `marking/mark.sh` and `mark.cmd` wrapper scripts, which break on the wrong line endings. Small and justified. Consider adding it to T1's `Files:` or to `pr.allowed_extra_paths`.
CLAUDE.md: ⚠️ WARN: undeclared. It was created in the baseline commit and edited on this branch (0641456, "CLAUDE.md remote account"). That edit records the GitHub remote account, which ties to T1's title "GitHub remote". Project docs rather than scope creep. Consider adding it to `pr.allowed_extra_paths`.
.specclaw/STATUS.md: ⚠️ WARN: SpecClaw bookkeeping, written by the "specclaw: build done, timeline" and "verify PASS" commits (1bdf964, ab5e6f9). It is a framework-generated ripple of the workflow itself. No action needed.

## Summary

The verdict is CHANGES_REQUESTED only because rule 5 makes any `required-missing` entry non-negotiable. The substance is that `proposal.md` and `design.md` already exist on the branch, unchanged, from baseline commit 31a2b40, so there is nothing to commit. The gate diffs against `main` and so cannot see files that were committed before the branch was cut. This is a gate false positive, and `workflow.staged_files_block` ships `false`, so the PR is not blocked unless the operator changed it. The operator may proceed, or amend the checker to also accept artifacts present at HEAD. None of the 37 undeclared paths is scope creep. 34 of them are covered by T5/T6 globs and by `templates/Reflect.java` in T1, which the checker's glob parsing missed, and `scripts/sync-papers.sh`, `.gitattributes`, `CLAUDE.md` and `.specclaw/STATUS.md` are named, justified ripples. No path is in `suspicious`, so no `.gitignore` change is needed.
