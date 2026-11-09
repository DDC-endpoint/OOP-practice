# Spec: LBP Practice Pack

**Change:** 001-lbp-practice-pack
**Created:** 2026-11-09
**Status:** 🟡 Draft

## Overview

A practice repo for the 5COSC019W OOP Lab-Based Practical. It teaches each skill the papers need, marks attempts automatically, and ends with an exam-format mock paper. Source design: `docs/design.md`, which states the intent "Get ready for the 5COSC019W Lab-Based Practical (LBP), worth 40%, in Week 9". Exam format, from `D:\year-2-prep\OOP\05_LBP_Practice_Papers\OOP_LBP_00_Student_Briefing_Format_and_Rules.docx`: "The working time is 100 minutes. Four source-code tasks comprise Section A (80 marks). Section B contains functional checks of the running program (20 marks)."

## Requirements

### Functional Requirements

- **FR-1 Drills.** Eleven drills, `drills/d01-*` … `drills/d11-*`, each a Maven module of the root aggregator, covering the skills in `docs/design.md` § Drill list, in that order. Each drill has:
  - `LESSON.md` with exactly these sections: `## The idea`, `## Worked example`, `## Your task`, `## Hints`. Hints use `<details>` blocks, from gentle to near-answer, at least 3.
  - A starter under `src/main/java/edu/practice/dNN/...` using the LBP package layout (`model / dto / repo / service / controller / error`, only the packages the drill needs). TODO bodies throw `UnsupportedOperationException("dNN: <what to do>")`.
  - Marking tests under `src/test/java/...`, runnable from IntelliJ or `mvnw -pl drills/dNN-* test`.
- **FR-2 Mock paper M1.** `mock-papers/M1-courier/` is a standalone Spring Boot project (its own `pom.xml` and wrapper) with a new fictional "Courier Parcel Pricing & Dispatch" scenario. It is in the exact LBP format: Task 1 (18), Task 2 (22), Task 3 (20), Task 4 (20), Section B (six checks, 20). It must exercise every skill d01–d10 teaches: a given subclass to finish, a new subclass to create, overflow-safe integer maths with ceiling rounding, polymorphic processing with a multi-key sort, greedy selection within a budget, validation, 400/404/409 via `ApiError`, a `@RequestParam` with a default, `@PathVariable`, `@RequestBody`, and 201 Created. The paper text is at `mock-papers/M1-courier/PAPER.md`.
- **FR-3 Past papers.** `past-papers/P1-parking`, `P2-drone`, `P3-scheduler` contain the original starters, unzipped and unchanged, plus the Maven wrapper.
- **FR-4 External marking.** `marking/<paper-id>/` holds marking tests for M1, P1, P2 and P3, written from each paper's stated marks and illustrative values. A runner `marking/Mark.java` (run with `java marking/Mark.java <paper-id> <projectDir>`, wrappers `mark.sh` / `mark.cmd`) does four things:
  1. copies the tests into the project's `src/test/java`
  2. runs `mvnw test`
  3. reads the surefire XML
  4. prints a marks table per task, and a total out of 100

  Marks are encoded in test method names as a `_m<N>` suffix. The marks per task must sum exactly to the paper's allocation.
- **FR-5 Section B as tests.** Section B checks are `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` tests against the running app.
- **FR-6 Solutions branch.** The `solutions` branch is `main` plus completed code for every drill, M1, P1, P2 and P3. `main` never contains solution bodies.
- **FR-7 Papers for the site.** `papers/<id>.md` exists for each drill (`d01`…`d11`), `m1`, `p1`, `p2` and `p3`. Each has YAML front matter `id, module: OOP, kind: drill|mock|past-paper, title, difficulty: 1-5, timeMinutes, tasks: [{name, marks}]`, followed by the brief text.
- **FR-8 README.** The root `README.md` covers clone, open and run on any device (IntelliJ Community, JDK 17), how to run a drill's tests, how to attempt and mark a paper, how to compare with `solutions` (`git diff main solutions -- <path>`), and a day-by-day order up to the LBP.
- **FR-9 Remote.** A private GitHub repo `EnderGuardian25/OOP-practice` with `main` and `solutions` pushed. Push after each wave, so the student can start on d01–d03 early.
- **FR-10 Self-check.** `scripts/selfcheck.sh` checks the whole pack in one command (see AC-1 to AC-3). `.specclaw/config.yaml` `build.test_command` points to it.

### Non-Functional Requirements

- **NFR-1** Java 17 / Spring Boot 3.3.5 / `spring-boot-starter-web` + `spring-boot-starter-test` only, as in the LBP starters. No other dependencies.
- **NFR-2** Builds with `JAVA_HOME=C:\Program Files\Java\jdk-17` through the Maven wrapper (Maven 3.9.9). No global Maven needed.
- **NFR-3** Lesson language is plain and short: w3schools-like, one idea per paragraph, code before theory.
- **NFR-4** Each drill takes 10–20 minutes for a student who has read its lesson.
- **NFR-5** Scenarios are fictional and new, and never copied from P1–P3. Nothing claims to predict the real exam.

## Acceptance Criteria

- [ ] **AC-1** On `main`, `mvnw test-compile` succeeds at the root (all drills) and in M1 and P1–P3. Every drill has at least one failing test, and no drill passes untouched.
- [ ] **AC-2** On `solutions`, `mvnw test` at the root passes every drill test.
- [ ] **AC-3** On `solutions`, `Mark.java` scores **100/100** for M1, P1, P2 and P3. On the untouched `main` starters it scores **≤ 20/100** for each.
- [ ] **AC-4** Marks encoded in each paper's marking tests sum to 18/22/20/20 for Section A and 20 for Section B, per paper. The runner fails loudly if they don't.
- [ ] **AC-5** Hard-coded answers fail. Every behavioural rule is checked with at least two inputs, and at least one of them is not shown in the lesson or paper.
- [ ] **AC-6** Where a paper or drill forbids type checks ("Do not use instanceof"), a test scans the named source file and fails on `instanceof` or `getClass()`.
- [ ] **AC-7** Classes the student must create (for example a second subclass) are reached through a test helper `Reflect` (reflection with a friendly "Create class X in package Y" failure). Starters and tests therefore compile before the class exists, and one missing class costs only its own marks.
- [ ] **AC-8** Every drill's `LESSON.md` has the four required sections and at least 3 hints. Every `papers/*.md` front matter parses as YAML, and has all FR-7 keys and correct task marks.
- [ ] **AC-9** The GitHub repo exists, is **private**, and has `main` and `solutions` matching local.
- [ ] **AC-10** `git diff main solutions --stat` touches only `src/main/**` files (and solution notes). Lessons, tests and marking files are identical on both branches.

## Edge Cases

- A student creates a class in the wrong package. `Reflect` names the expected fully qualified name.
- A student's code doesn't compile. In a drill, IntelliJ shows it in that module. For marking, the runner reports "compile failed → 0 for Section A/B" and shows the first compiler error. It never crashes silently.
- Marking is run twice on the same project. Copying the tests overwrites the previous copy, so the run stays repeatable.
- Windows paths with spaces (`C:\Program Files\...`). The wrappers quote all paths.
- A port is already in use. Section B tests use `RANDOM_PORT`, never 8080.
- Section B tests mutate in-memory state (POST). Use `@DirtiesContext` or explicit ordering, so tests don't depend on run order.

## Dependencies

- JDK 17 (installed). The Maven 3.9.9 distribution is downloaded once to generate the wrapper.
- `gh` authenticated as `EnderGuardian25` with `repo` scope (verified).
- The source papers and starters in `D:\year-2-prep\OOP\05_LBP_Practice_Papers\`.

## Notes

- Assumption: the student uses IntelliJ IDEA Community, so no HTTP Client is available. Section B is covered by tests, and each paper also gets a `requests.http` as a bonus for Ultimate users.
- d11 stays one drill. Splitting it is unnecessary at this size (open question resolved).
