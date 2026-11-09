# Design: LBP Practice Pack

**Change:** 001-lbp-practice-pack
**Created:** 2026-11-09

## Technical Approach

Content and code live in one repo with two branches:
- **`main`**: starters, lessons, tests, marking and papers.
- **`solutions`**: `main` merged in, plus completed `src/main` bodies.

Each build task first writes the starter, lesson and tests on `main`, commits, then switches to `solutions`, merges `main`, fills in the bodies, makes the tests green and commits. The diff between the branches is therefore exactly the solutions (AC-10).

The drills share one aggregator, so one IntelliJ "Open" shows them all. The mock and past papers are standalone, like the real exam. Marking for papers is external and gives a score.

## Architecture

```
OOP-practice/
├─ pom.xml                     aggregator, parent spring-boot-starter-parent 3.3.5,
│                              groupId edu.practice, artifactId oop-practice, packaging pom,
│                              <modules> drills/d01-… … drills/d11-…
├─ mvnw, mvnw.cmd, .mvn/       Maven 3.9.9 wrapper (also copied into each standalone project)
├─ drills/dNN-<slug>/
│   ├─ pom.xml                 parent = edu.practice:oop-practice, artifactId dNN-<slug>
│   ├─ LESSON.md
│   ├─ src/main/java/edu/practice/dNN/…
│   └─ src/test/java/edu/practice/dNN/…  (+ edu/practice/testkit/Reflect.java, copied per module)
├─ mock-papers/M1-courier/     standalone project + PAPER.md + requests.http
├─ past-papers/P1-parking | P2-drone | P3-scheduler/   original starter + wrapper + PAPER.md
├─ marking/
│   ├─ Mark.java               single-file Java runner (java marking/Mark.java <id> <dir>)
│   ├─ mark.sh, mark.cmd       thin wrappers
│   └─ m1/ p1/ p2/ p3/         test sources, laid out as src/test/java/... to copy over
├─ papers/<id>.md              front matter + brief, for the future site
├─ scripts/selfcheck.sh        AC-1..AC-4 in one run (uses git worktree for solutions)
├─ docs/design.md              approved design (source of this change)
├─ README.md, CLAUDE.md, HANDOFF.md
```

### Drill line-up (each scenario new and fictional)
| Id | Slug | Skill | Scenario |
|---|---|---|---|
| d01 | classes-constructors | abstract parent, `final` fields, `super(...)`, getters, validation in constructor | Library items (`LibraryItem` → `Book`) |
| d02 | new-subclass | create a subclass from scratch, override an abstract method (student creates the class; tests use `Reflect`) | Gym memberships (`Membership` → `PayAsYouGo` given, student creates `Annual`) |
| d03 | integer-maths | ceiling division, tiered pricing, `Math.multiplyExact` / `addExact` → `IllegalArgumentException` on overflow, rejecting non-positive values | Cloud storage billing (per started 100 GB block, tier above 10 blocks) |
| d04 | polymorphism | loop over `List<Parent>`, call the overridden method, no `instanceof` (source scan) | Delivery vehicles' fuel cost |
| d05 | sorting | `Comparator.comparing(...).thenComparing(...)`, `reversed()`, streams `filter` / `map` / `sorted` / `toList` | Tournament leaderboard (points DESC, goal diff DESC, name ASC) |
| d06 | records-dtos | records as DTOs, mapping model → DTO in a private helper | Hotel rooms → `RoomQuote` |
| d07 | service-validation | positive, blank, `trim`, unique ID, custom `XNotFoundException`, repository use | Course enrolment service |
| d08 | error-handling | `@RestControllerAdvice`, `@ExceptionHandler`, `ResponseEntity<ApiError>`, 400/404/409 (MockMvc) | Ticket desk API |
| d09 | rest-endpoints | `@GetMapping` / `@PostMapping`, `@PathVariable`, `@RequestParam(defaultValue=…)`, `@RequestBody`, 201 (TestRestTemplate) | Plant nursery API |
| d10 | debug-and-check | the app is complete but has 3 planted bugs (wrong status, off-by-one ceiling, unsorted list). Student runs it, curls it, reads the stack trace and fixes it. The lesson covers Section B technique and the ZIP checklist | Cinema seat pricing |
| d11 | stateful-commit | preview vs commit, idempotent retry by key (same request → same result, changed request → 409), half-open interval overlap | Meeting-room bookings |

### Marking runner contract (`marking/Mark.java`)
- Arguments: `<paper-id>` (`m1|p1|p2|p3`) and `<projectDir>`. Steps:
  1. Delete `<projectDir>/src/test/java/marking/`, then copy `marking/<id>/src/test/java/marking/**` into it.
  2. Run `<projectDir>/mvnw(.cmd) -q test -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.test.failure.ignore=true`.
  3. Parse `<projectDir>/target/surefire-reports/TEST-*.xml`.
- Marks come from method names ending in `_m<N>`. Tasks come from class names: `Task1Test` … `Task4Test`, `SectionBTest`.
- Output: one line per test (✔/✘, marks, name), task subtotals out of 18/22/20/20/20, and the total out of 100.
- If the declared marks per class don't equal the allocation, print `MARKING ERROR` and exit 2. If compilation fails, print the first `[ERROR]` lines plus "compile failed → 0" and exit 1.
- Exit code 0 means it ran, whatever the score.

### `Reflect` test helper
`edu.practice.testkit.Reflect` (P1–P3/M1: `marking.Reflect`):
- `Class<?> require(String fqcn)`: fails with "Create class `<simple>` in package `<pkg>`".
- `Object construct(String fqcn, Object... args)`: matches a public constructor by arity and assignable/primitive-boxed types.
- `Object call(Object target, String method, Object... args)`: unwraps `InvocationTargetException` so `assertThrows(IllegalArgumentException.class, …)` works.

It's used only for classes the student creates. Given classes are referenced directly.

## File Changes Map

| File | Action | Description |
|------|--------|-------------|
| `pom.xml`, `mvnw*`, `.mvn/` | Create | aggregator + wrapper |
| `.gitignore` | Modify | add `target/`, `.idea/`, `*.iml`, `out/` |
| `drills/d01…d11/**` | Create | 11 drill modules |
| `mock-papers/M1-courier/**` | Create | mock paper project + PAPER.md + requests.http |
| `past-papers/P1…P3/**` | Create | unzipped original starters + wrapper + PAPER.md (converted text) |
| `marking/**` | Create | runner, wrappers, m1/p1/p2/p3 marking tests |
| `papers/*.md` | Create | 15 front-matter briefs |
| `scripts/selfcheck.sh` | Create | whole-pack check |
| `README.md` | Create | usage + schedule |
| `.specclaw/config.yaml` | Modify | `build.test_command: "bash scripts/selfcheck.sh"` |
| `HANDOFF.md` | Modify | end of build |

## Data Model Changes

None (no database). `papers/*.md` front matter is the one interface the future site depends on:
```yaml
---
id: d03            # d01..d11 | m1 | p1..p3
module: OOP
kind: drill        # drill | mock | past-paper
title: Integer pricing maths
difficulty: 2      # 1-5
timeMinutes: 15
tasks:
  - { name: "Ceiling division", marks: 4 }
---
```
Drills use their own marks (sum ≈ 10). Papers use 18/22/20/20/20.

## API Changes

None outside the practice projects themselves. M1 endpoints are defined in `PAPER.md` by T6.

## Key Decisions

- **External, scored marking for papers.** It mirrors the exam (no visible tests) and gives the student a number to track. Marks live in method names, so no extra libraries are needed (NFR-1).
- **The runner is single-file Java**, not shell. It's cross-platform with only JDK 17, which resolves the design's open question.
- **Reflection only for student-created classes.** It keeps compilation green and gives partial credit (AC-7). Everything else uses plain typed code, so the tests stay readable as examples.
- **Selfcheck uses `git worktree add ../.oop-solutions solutions`.** That way it checks both branches without switching the working tree.
- **d11 stays one drill.**

## Risks & Mitigations

- **Time (LBP next week).** Build waves in priority order. Push after the first drill wave so the student starts on d01–d03 while the rest is built.
- **Tests too loose (anti-goal).** AC-5 (≥2 inputs, one unseen) and AC-3 (starters ≤ 20/100) are enforced by selfcheck.
- **Our P1–P3 marking differs from the staff marking.** Marking follows only the paper's stated rules and illustrative values. The README says our score is an estimate.
- **Branch drift.** Always merge `main` → `solutions`, never the reverse. AC-10 is checked in selfcheck.
- **The first wrapper run downloads Maven**, which needs a network connection. The README notes it.
