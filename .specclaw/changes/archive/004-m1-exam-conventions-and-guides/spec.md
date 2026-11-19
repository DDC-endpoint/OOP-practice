# Spec: Rework M1 to the exam's format and conventions; add exam-day and refresher guides

**Change:** 004-m1-exam-conventions-and-guides · **Size:** architectural · **Source:** `proposal.md`

## Functional requirements
- **FR-1 (M1 conventions):**
  - The M1 starter packages are exactly `controller, exception, model, repository, service`, plus the app class in the base package. There's no `dto` package and no records.
  - `Quote`, `VanLoad` and `NewStandardParcel` are plain classes in `model` with private fields and getters. `NewStandardParcel` also has a public no-arg constructor and setters.
  - `GlobalExceptionHandler`, `ErrorResponse` (a plain class with getters), `ParcelNotFoundException` and `DuplicateParcelException` live in `exception`, and `ParcelRepository` in `repository`.
- **FR-2 (M1 HTTP):** the controller maps `/parcels`, `server.port=8080`, and springdoc 2.6.0 serves `/swagger-ui.html`. The JSON field names are unchanged.
- **FR-3 (M1 paper):**
  - `PAPER.md` describes a 120-minute exam: 110 for coding and testing, plus the final 10 for the ZIP + Blackboard upload.
  - Q1 Section A = Tasks 1–3 (60), Q2 Section B = Task 4 (20), Section C = six functional checks (20). The task marks are unchanged.
  - It covers Swagger-based checking, the "exactly ONE new class (ExpressParcel)" rule, the no-records/no-dto rule, and the exam-day rules.
  - The URLs and class names match FR-1/FR-2. The rules, formulas, seeds and illustrative values are unchanged.
- **FR-4:** `README.md`, `requests.http`, `papers/m1.md` and `PAPER.pdf` all agree with FR-1..3.
- **FR-5 (marking):** `marking/m1` compiles against the new names, uses getters and the `/parcels` URLs, and its functional class is `SectionCTest`. `Mark.java` labels it "Section C" for m1. p1–p3 keep `SectionBTest`.
- **FR-6 (solutions):** the `solutions` branch carries the solved M1 in the new shape.
- **FR-7 (guides):** `guides/EXAM-DAY.md` and `guides/REFRESHER.md` are rendered to `guides/*.pdf` by `scripts/build-pdfs.sh` and checked by `check.mjs`. The root README links both.

## Acceptance criteria
- **AC-1:** `bash scripts/selfcheck.sh` (run from a clean worktree of this branch) is OK. That includes: the M1 untouched starter ≤ 20, M1 solutions = 100, p1–p3 unchanged.
- **AC-2:** No `record ` and no `package ...dto`/`.error`/`.repo` remain under `mock-papers/M1-courier/src`.
- **AC-3:** With the M1 solution running, `/swagger-ui.html` and `/v3/api-docs` answer 200 on port 8080.
- **AC-4:** `PAPER.md` contains "120", "110", "Section C", "Swagger", "ONE new", "Blackboard", and no `/api/parcels` or `8084`.
- **AC-5:** `build-pdfs.sh` builds and checks `guides/EXAM-DAY.pdf` and `guides/REFRESHER.pdf` with PASS.
- **AC-6:** The diff stays inside the design's file map.

## Edge cases
- Jackson needs a no-arg constructor + setters for the request class, and getters for responses. Otherwise a POST answers 400 or the body serialises to `{}`.
- Swagger's request-body example is generated from the class's getters/setters. It must show the 4 fields.
