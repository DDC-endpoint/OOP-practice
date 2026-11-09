# Proposal: LBP Practice Pack

**Created:** 2026-11-09
**Status:** 🟡 Draft

## Problem

The 5COSC019W Object Oriented Programming Lab-Based Practical (LBP), worth 40%, takes place in Week 9, around 16–20 Nov 2026, so roughly one week away. It is a 100-minute timed task in IntelliJ on a supplied Java 17 / Maven / Spring Boot starter. Marks: Section A has four coding tasks (18/22/20/20), Section B has six functional checks on the running app (20), and a viva follows.

The student attempted practice paper P1 and got stuck on every part: model maths, polymorphic sorting, validation and HTTP error mapping, and the REST endpoints with their functional checks. The three official practice papers (P1 simple, P2 moderate, P3 extreme) expect skills that the lectures don't clearly teach: ceiling division, overflow-safe arithmetic, two-key comparators, `@RequestParam`, 201/400/404/409 mapping, input validation, idempotent commit, and exam technique. Nothing currently marks an attempt, so the student can't tell whether their work is right.

## Proposed Solution

Build a new repo at `D:\OOP-practice`, synced through a private GitHub repo, that **teaches first, then tests**. Full design: `docs/design.md` (approved 2026-11-09).

- **Drills d01–d11:** one skill each, 10–20 minutes, ordered easy to hard. Each is a Spring Boot module of a single Maven aggregator. It has a `LESSON.md` (concept, worked example, task, progressive hints), a starter with TODOs in the LBP starter style, and marking tests.
- **Mock paper M1:** a standalone project with a new fictional scenario in the exact LBP format (four tasks at 18/22/20/20 plus six Section B checks). It covers every aspect from P1–P3. Its marking tests are kept outside the project and applied by script after the 100 minutes.
- **P1–P3 marking:** the original starters plus external marking tests, applied the same way.
- **Section B checks** run as `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` tests, which start the real app.
- **Model solutions** live on a `solutions` branch. `main` holds starters, lessons and tests only.
- **Every paper and drill brief** is also written to `papers/*.md` with front matter, so the planned study site can import it.

## Scope

### In Scope
- Repo scaffold: aggregator `pom.xml`, Maven wrapper in every project, `.gitignore`, README with clone/open/run steps and a suggested day-by-day order up to the LBP.
- Drills d01–d11 (skills per `docs/design.md` drill table), each with lesson, starter, marking tests, and a solution on `solutions`.
- Mock paper M1: paper, starter, external marking tests (Section A + B), and a solution on `solutions`.
- P1–P3: starters copied in, external marking tests written from each paper's stated marks and illustrative values, and solutions on `solutions`.
- Cross-platform marking scripts (`.sh` + `.cmd`) that copy marking tests into a paper project and run them.
- `papers/*.md` with front matter (`id, module, kind, title, difficulty, timeMinutes, tasks[{name, marks}]`).
- Private GitHub repo `OOP-practice` created with `gh`; `main` and `solutions` pushed.

### Out of Scope
- The study website, auth, VPS hosting, Google Calendar link, and in-browser code running.
- ACSD (TypeScript/React) and DB content.
- Mock papers beyond M1.
- Any claim to predict the real exam.

## Impact

- **Size:** architectural (spike / bounded / architectural). This is a new repo and subsystem, and its `papers/*.md` format is an interface the future site will depend on.
- **Files affected:** ~250 (estimated: 15 projects × ~12 Java files, plus lessons, tests, scripts, papers)
- **Complexity:** large (small / medium / large)
- **Risk:** medium (low / medium / high). The deadline is tight, and loose tests could let wrong solutions pass (design anti-goal).

## Open Questions

- Exact LBP date. Week 9 is estimated at 16–20 Nov 2026. The student confirms the LBP is "next week".
- Whether d11 (P2/P3-level stateful logic) is split into two drills. Left to the builder per design.
- Marking-script runtime: `.sh` + `.cmd` pair vs a tiny Java runner. Left to the builder per design.

---

**To proceed:** Review this proposal and approve to begin planning.
