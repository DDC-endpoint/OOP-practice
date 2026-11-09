# Tasks: LBP Practice Pack

**Change:** 001-lbp-practice-pack
**Created:** 2026-11-09
**Total Tasks:** 9

## Summary

Build in priority order so the student can start drilling early:
- Wave 1: scaffold, runner and remote.
- Wave 2: drills d01–d03, then push.
- Wave 3: drills d04–d10.
- Wave 4: M1, P1–P3 marking, d11.
- Wave 5: README schedule, selfcheck run, final push.

Every content task follows the same branch routine:
1. On `main`, write the starter, lesson, tests and paper md, check `test-compile`, and commit.
2. `git switch solutions && git merge main`, fill in the bodies, run the tests green, and commit.
3. `git switch main`.

Builds use `JAVA_HOME="C:/Program Files/Java/jdk-17"`.

## Tasks

### Wave 1 — Scaffold and tooling

- [x] `T1` — Repo scaffold, wrapper, Reflect helper, marking runner, selfcheck, GitHub remote
  - Files: `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.gitignore`, `marking/Mark.java`, `marking/mark.sh`, `marking/mark.cmd`, `scripts/selfcheck.sh`, `templates/Reflect.java` (source copied into each project), `.specclaw/config.yaml`
  - Estimate: medium
  - Kind: config
  - Notes:
    - Download Maven 3.9.9 to the scratchpad and run `mvn -N wrapper:wrapper -Dmaven=3.9.9 -Dtype=only-script`.
    - The aggregator `<modules>` starts empty and is filled by later tasks.
    - `Mark.java` follows the design's "Marking runner contract". Verify it on a throwaway project with one `_m5` passing test and one `_m3` failing test → prints 5/8. Also verify that the allocation-mismatch path exits 2.
    - `selfcheck.sh` steps:
      1. root `test-compile` on `main`
      2. each drill has ≥1 failing test on `main`
      3. worktree `solutions` → root `test` green
      4. `Mark.java` for each paper on solutions = 100 and on main ≤ 20
      5. `git diff main solutions --name-only` touches only `src/main/` paths
      6. front-matter keys exist in `papers/*.md`

      Steps for parts not yet built are skipped with a "SKIP (not built)" line.
    - Set `build.test_command: "bash scripts/selfcheck.sh"`.
    - Create branch `solutions` from the first commit.
    - `gh repo create DDC-endpoint/OOP-practice --private --source . --push`, then push `solutions`.

### Wave 2 — First drills (student starts here)

- [x] `T2` — Drills d01 classes-constructors, d02 new-subclass, d03 integer-maths
  - Files: `drills/d01-classes-constructors/**`, `drills/d02-new-subclass/**`, `drills/d03-integer-maths/**`, `papers/d01.md`, `papers/d02.md`, `papers/d03.md`, `pom.xml` (modules)
  - Estimate: large
  - Kind: impl
  - Depends: T1
  - Notes:
    - Scenarios and skills as in the design's drill line-up. Each `LESSON.md` has `## The idea`, `## Worked example` (using a *different* mini-example from the task), `## Your task`, `## Hints` (≥3 `<details>`), `## Viva questions` (3–5, answers in `<details>`).
    - d02: the student creates `edu.practice.d02.model.Annual`, and its tests reach it only through `Reflect`.
    - d03 tests must include: exact-boundary values (100 → 1 block, 101 → 2), the tier boundary, 0 and negative → `IllegalArgumentException`, and `Integer.MAX_VALUE` input → `IllegalArgumentException` (overflow), never a wrapped negative.
    - Every rule is tested with ≥2 inputs, at least one not in the lesson (AC-5).
    - Push `main` + `solutions` at the end and tell the student d01–d03 are ready.

### Wave 3 — Remaining core drills

- [x] `T3` — Drills d04 polymorphism, d05 sorting, d06 records-dtos
  - Files: `drills/d04-polymorphism/**`, `drills/d05-sorting/**`, `drills/d06-records-dtos/**`, `papers/d04.md`, `papers/d05.md`, `papers/d06.md`, `pom.xml`
  - Estimate: large
  - Kind: impl
  - Depends: T2
  - Notes:
    - d04 includes the source-scan test (no `instanceof` / `getClass()` in the service file; AC-6).
    - d05 tests tie-breaks explicitly: equal primary key → secondary key → tertiary key.
- [x] `T4` — Drills d07 service-validation, d08 error-handling, d09 rest-endpoints
  - Files: `drills/d07-service-validation/**`, `drills/d08-error-handling/**`, `drills/d09-rest-endpoints/**`, `papers/d07.md`, `papers/d08.md`, `papers/d09.md`, `pom.xml`
  - Estimate: large
  - Kind: impl
  - Depends: T2
  - Notes:
    - d07: trim before storing, blank after trim → 400-worthy `IllegalArgumentException`, duplicate ID rejected and **not persisted**.
    - d08: MockMvc asserts both the status and the JSON body `code` / `message`.
    - d09: TestRestTemplate, a `@RequestParam` default value tested by omitting the param, POST → 201 and the item appears in a later GET.
- [~] `T5` — Drill d10 debug-and-check
  - Files: `drills/d10-debug-and-check/**`, `papers/d10.md`, `pom.xml`
  - Estimate: medium
  - Kind: impl
  - Depends: T2
  - Notes:
    - On `main` the app is complete except for 3 planted bugs. The tests expose the symptoms only, never the line.
    - The lesson teaches: start the app, curl or Postman commands (Windows `curl.exe` syntax as well), reading a stack trace top-down to the first `edu.practice` frame, the Section B mindset, and the ZIP checklist (pom.xml + src, extract and test).

### Wave 4 — Papers

- [~] `T6` — Mock paper M1 courier
  - Files: `mock-papers/M1-courier/**` (standalone project + wrapper + `PAPER.md` + `requests.http`), `marking/m1/**`, `papers/m1.md`
  - Estimate: large
  - Kind: impl
  - Depends: T1, T3, T4
  - Notes:
    - Must satisfy FR-2. Write `PAPER.md` in the LBP paper structure (instructions, scenario, files supplied, marking table, Tasks 1–4 with per-line marks, illustrative calculations, REST operations, Section B six checks, submission).
    - The starter mirrors the LBP starter style: an abstract parent, one incomplete subclass, a repo with a seed TODO, DTO records, a service and controller with TODOs, and an `ApiExceptionHandler` with a TODO.
    - Marking tests: `marking/m1/src/test/java/marking/{Task1Test..Task4Test,SectionBTest,Reflect}.java`. Their marks match `PAPER.md` line by line.
- [~] `T7` — P1–P3 projects and marking
  - Files: `past-papers/P1-parking/**`, `past-papers/P2-drone/**`, `past-papers/P3-scheduler/**` (unzipped from `D:\year-2-prep\OOP\05_LBP_Practice_Papers\*_Starter.zip`, plus wrapper and `PAPER.md` converted from the docx), `marking/p1/**`, `marking/p2/**`, `marking/p3/**`, `papers/p1.md`, `papers/p2.md`, `papers/p3.md`
  - Estimate: large
  - Kind: impl
  - Depends: T1
  - Notes:
    - The marking tests follow each paper's mark lines exactly. Every illustrative value in the paper becomes an assertion (P1: 61 min → 500, 481 min → 950, WeekendPass first at 481; P2: energy 39/38, plan capacity=60 → selected [103,102], skipped [101], used 56, remaining 4; P3: the deadline workload ranks ahead at tick 0, identical commit leaves the reservation count unchanged, a changed runKey request → 409).
    - Solutions on `solutions` reach 100/100.
    - P3 is the stretch paper. If time runs short, ship P3's marking and leave its solution for last.
- [~] `T8` — Drill d11 stateful-commit
  - Files: `drills/d11-stateful-commit/**`, `papers/d11.md`, `pom.xml`
  - Estimate: medium
  - Kind: impl
  - Depends: T4
  - Notes:
    - Tests: preview called twice leaves the confirmed state unchanged; same key + same request returns an equal result and the count is unchanged; same key + different request → 409 `CONFLICT`; `[9,11)` and `[11,12)` don't overlap, but `[9,11)` and `[10,12)` do.

### Wave 5 — Finish

- [ ] `T9` — README, selfcheck green, final push, handoff
  - Files: `README.md`, `HANDOFF.md`
  - Estimate: small
  - Kind: docs
  - Depends: T2, T3, T4, T5, T6, T7, T8
  - Notes:
    - README per FR-8, with the day-by-day order from today to the LBP: d01–d03 → d04–d06 → d07–d09 → d10 + P1 timed → P2 timed + M1 timed under exam conditions → review weak drills + P3 optional.
    - Run `bash scripts/selfcheck.sh`, which must pass all of AC-1..AC-4 and AC-8..AC-10.
    - Push both branches and verify AC-9 with `gh repo view --json visibility`.

---

## Legend

- `[ ]` Pending
- `[~]` In Progress
- `[x]` Complete
- `[!]` Failed
- `[>]` Deferred — correctly blocked on a sibling change, not incomplete through any fault of its own; excluded from the incomplete-task count that gates `verify`

**Task format:**
```
- [ ] `T<n>` — <title>
  - Files: <files to create/modify>
  - Estimate: small | medium | large
  - Kind: docs | test | config | refactor | impl | migration   (optional; hints the build subagent's role, tools, and model)
  - Depends: <task ids> (if any)
  - Notes: <additional context>
  - Deferred-Reason: <why this can't be built yet>            (required when marker is `[>]`)
  - Deferred-Blocked-On: <sibling change name, if known>      (optional; free text, not a structured link)
```
