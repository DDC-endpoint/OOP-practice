# OOP Practice Pack — Design

Date: 2026-11-09 · Status: draft, awaiting approval

## Intent
Get ready for the 5COSC019W Lab-Based Practical (LBP), worth 40%, in Week 9 (around 16–20 Nov 2026). The LBP is a 100-minute timed task in IntelliJ on a Java 17 / Maven / Spring Boot starter project, and you submit a ZIP of the project. It is marked as Section A (four coding tasks: 18/22/20/20) plus Section B (six functional checks on the running app, 20 marks), with a viva afterwards.

The student attempted practice paper P1 and got stuck on every part: model maths, polymorphic sorting, validation and error mapping, and the REST endpoints. Several of the skills the papers expect are not clearly covered in the lectures. The pack must therefore **teach first, then test**, and finish with an exam-format mock.

This pack is separate from the planned study site. The site is designed after the LBP and will import this pack's papers.

## Goals
- Close each gap with a short, focused drill that is marked automatically, so the student knows when they've got it right.
- Provide one full mock paper (M1) in the exact LBP format, with a new scenario that covers every aspect from P1–P3.
- Make P1–P3 self-markable with tests.
- Work on any device with IntelliJ: clone, open, run.
- Store every paper as structured Markdown so the future site can import it.

## Non-goals
- No website, login or VPS work now.
- No in-browser code editor. Practice happens in IntelliJ, as in the exam.
- No coverage of ACSD or DB in this pack.

## Anti-goals
- Drills that can be passed without understanding, for example tests so loose that hard-coded return values pass.
- Solutions visible by default. The student must be able to attempt cleanly.
- Content that predicts or claims to be the real exam.

## Constraints
- Match the starters exactly: Java 17, Spring Boot 3.3.5, `spring-boot-starter-web` + `spring-boot-starter-test`, package layout `model / dto / repo / service / controller / error`, records for DTOs, in-memory `ConcurrentHashMap` repository, `ApiError(code, message)`.
- Maven isn't installed on this PC, so every project ships the Maven wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`).
- IntelliJ's HTTP Client is Ultimate-only, so Section B checks must run as JUnit tests (`@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`). A `.http` file is a bonus, not the main check.

## Approaches considered
1. **Drills + mock (chosen).** About 10 one-skill drills, then a full 100-minute mock, plus P1–P3 with marking tests. This fixes gaps one at a time, then tests whether the student can combine them under time. Costs more build time.
2. **Single mock paper only.** Faster to build, but the student would hit the same walls as in P1 with nothing to teach the missing skill. Rejected.

## Decisions (made with the student)
- Approach 1.
- Location `D:\OOP-practice`, synced through a **private GitHub repo** (`OOP-practice`) created with `gh`.
- Model solutions live on a **`solutions` branch**. `main` holds starters, lessons and tests only.

## Layout (builder's call, see below)
```
OOP-practice/
  README.md                 how to clone, open, run tests, suggested order and schedule to the LBP
  pom.xml                   aggregator: one IntelliJ window opens all drills
  drills/
    d01-.../                each a Spring Boot module:
      LESSON.md             concept → worked example → your task → hints (progressive)
      src/main/...          starter with TODOs, same style as the LBP starters
      src/test/...          marking tests (unit + MockMvc / TestRestTemplate)
  mock-papers/M1-.../       standalone project (open on its own, like the exam)
  past-papers/P1|P2|P3/     original starters, plus marking tests kept outside src/
  marking/                  scripts that copy marking tests into a paper project and run them
  papers/*.md               every paper and drill brief as Markdown with front matter, for the site
  docs/design.md            this file
```

## Drill list (draft, ordered easy → hard)
| # | Skill | P1 link |
|---|---|---|
| d01 | Abstract parent, `final` fields, constructor chaining with `super(...)`, getters | Task 1 |
| d02 | Creating a subclass from scratch and overriding an abstract method | Task 2 |
| d03 | Integer pricing maths: ceiling division, tiers, `Math.multiplyExact` / `addExact`, `IllegalArgumentException` | Tasks 1–2 |
| d04 | Polymorphism over `List<Parent>` with no `instanceof` | Task 2 |
| d05 | Sorting: `Comparator.comparing(...).thenComparing(...)`, `reversed()`, streams `map` / `sorted` / `toList` | Task 2 |
| d06 | Records and DTOs: building response records from models, private helper methods | Tasks 2–3 |
| d07 | Service validation: positive, blank, `trim`, unique ID; repository use; custom exceptions | Task 3 |
| d08 | Error mapping: `@RestControllerAdvice`, `@ExceptionHandler`, `ResponseEntity<ApiError>`, 400/404/409 | Task 3 |
| d09 | REST: `@GetMapping` / `@PostMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, 201 Created | Task 4 |
| d10 | Exam technique: running and checking the app (curl, Postman), Section B thinking, building and checking the ZIP | Section B |
| d11 | Stretch (P2/P3 level): stateful service logic, preview vs commit, idempotent retry, half-open intervals | P2/P3 |

## Calls I made (check these)
- Drills are modules of one aggregator project, so a single clone and a single "Open" shows everything. The mock and past papers are standalone, to rehearse the real exam setup.
- Drill tests ship on `main`, because they're learning tools. **Mock and past-paper marking tests are kept outside the project** and applied by a script after the 100 minutes, as in the real exam, where you don't see the tests.
- Tests check behaviour (values, status codes, ordering, error bodies) and source rules where the paper does, for example "no `instanceof`", checked by scanning the source.
- Papers in `papers/*.md` use front matter (`id, module, kind, title, difficulty, timeMinutes, tasks[{name, marks}]`) so the site can import them.

## Left to the builder
- Exact drill scenarios (new, fictional, not copied from P1–P3), test design, and lesson wording, in a clear w3schools-like teaching style.
- Whether d11 is split in two.
- The marking-script language: a cross-platform choice that works on Windows and other OSes (for example both `.sh` and `.cmd`, or a tiny Java runner).
