# Proposal: Rework M1 to the real exam's format and conventions, and add exam-day and refresher guides

**Created:** 2026-11-19
**Status:** 🟡 Draft

## Problem

The LBP is tomorrow (20 Nov 2026). The student sits M1 tonight on this PC as their last full rehearsal. Two documents arrived today that M1 doesn't match:

1. **The official exam-day instructions** (`D:\year-2-prep\OOP\05_LBP_Practice_Papers\OOP_LBP_00_Exam_Day_Instructions.md`):
   - 120 min = 110 for coding and testing + the **final 10 for the ZIP and the Blackboard upload**
   - Q1 Section A (Java/OOP) → Q2 Section B (Spring Boot/REST) → **Section C Functional Verification**
   - the starter includes **Swagger UI**
   - **exactly ONE new Java class**
   - no notes, no AI, no earlier code of your own

   M1 says 100 min, Section A 80 / Section B 20, and curl/HTTP Client.
2. **The module team's own mock** (`MODERATE_Mock_Practice_Paper.docx`, Emergency Dispatch):
   - Java 25 + Spring Boot 4.1.1
   - packages `controller, exception, model, repository, service`, with *"Do not create a dto package"*
   - *"All request and response model types must be ordinary Java classes, not Java records"*
   - the exception package holds `GlobalExceptionHandler` and `ErrorResponse`
   - URLs with no `/api` prefix, on `http://localhost:8080`
   - Swagger at `/swagger-ui.html`

   M1 uses `dto/` records, `error/ApiExceptionHandler` + `ApiError`, `repo/`, `/api/parcels`, port 8084, and no Swagger.

A rehearsal in the wrong shape trains the wrong habits, especially records. A plain class with no getters serialises to `{}`, and that's a silent Section C failure.

The student also asked for two documents:

- a **properly organised exam-day instructions PDF** in this repo
- a **refresher sheet** of the concepts, to read on the way to the exam (not in the room: notes are banned)

## Proposed Solution

### A. M1 rework (student's choice: format + conventions)
- **Packages:** `dto/*` → `model/` as plain classes (private fields, constructor, getters; the request class `NewStandardParcel` also gets a no-arg constructor + setters for Jackson). `error/` → `exception/`, `ApiExceptionHandler` → `GlobalExceptionHandler`, `ApiError` → `ErrorResponse`. `repo/` → `repository/`.
- **URLs and Swagger:** drop the `/api` prefix (`/parcels...`), and set `server.port=8080`. Add springdoc `2.6.0` (M1 stays on Boot 3.3.5 + JDK 17, because JDK 25 isn't on this PC). Keep the JSON field names unchanged.
- **`PAPER.md`:**
  - 120 min with 110 + 10
  - Q1 Section A = Tasks 1–3, Q2 Section B = Task 4, Section C = the six functional checks
  - the task marks stay unchanged (18/22/20/20/20)
  - Swagger is the checking tool
  - the "exactly one new class" rule (ExpressParcel), the no-records/no-dto rule, and the exam-day rules
  - ZIP + Blackboard in the last 10
- **Also updated:** `README.md`, `requests.http` and `papers/m1.md`, and `PAPER.pdf` is rebuilt.
- **`solutions` branch:** port the solved M1 files to the new names and classes.
- **Marking:**
  - `marking/m1` tests get the new imports, getters and URLs. `SectionBTest` becomes `SectionCTest`, and `Mark.java`'s allocation/label is updated for m1. p1–p3 keep "Section B".
  - selfcheck must still show the untouched M1 starter ≤ 20 and the solution exactly 100.

### B. `guides/EXAM-DAY.md` → `guides/EXAM-DAY.pdf` (full guide)
It covers:
- a minute-by-minute timeline (0–5 open the starter, 5–110 Q1 → Q2 → Section C checks, 110–120 ZIP + upload)
- allowed/banned lists and the code rules
- Swagger how-to
- ZIP + Blackboard steps
- the laptop setup checklist (JDK 25, IntelliJ Ultimate licence, AI plugins off, a dry run, pausing Windows Update)
- the module-mock conventions

Linked from the root `README.md`.

### C. `guides/REFRESHER.md` → `guides/REFRESHER.pdf`
A phone-readable concept sheet, a few pages long:
- inheritance/`super(...)`/`@Override`/abstract
- polymorphism with no `instanceof`
- encapsulation, and plain classes + getters for JSON
- integer maths (ceiling `(a-1)/b+1`, multiply before divide, `long` + `Math.toIntExact`)
- validation → `IllegalArgumentException`
- Comparator chains and where `.reversed()` goes
- streams (filter/map/sorted/toList)
- the greedy fill with a skipped list
- the repository Map and `putIfAbsent`
- the Spring annotation table, 201 Created, `@RestControllerAdvice`/`@ExceptionHandler`
- the status-code table, the Swagger check loop, viva one-liners

### D. The PDF builder
Extend `scripts/pdf` to render `guides/*.md` (as a `guide` kind, styled like a paper), and teach the checker that kind.

## Scope

### In Scope
- `mock-papers/M1-courier/**` (the starter `src`, `pom.xml`, `application.properties`, `PAPER.md/.pdf`, `README.md`, `requests.http`)
- `marking/m1/**` and `marking/Mark.java` (m1 label only)
- `papers/m1.md`
- `guides/EXAM-DAY.md/.pdf`, `guides/REFRESHER.md/.pdf`
- `scripts/pdf/{lib,build,check}.mjs`
- the root `README.md` link
- the `solutions` branch's M1 `src/main` files

### Out of Scope
- Moving M1 to Spring Boot 4.1.1 / JDK 25 (it isn't installed on this PC).
- P1–P3, the drills (change 003 handles d10/d11), and the module's MODERATE mock (the student does it themselves).
- Changing any M1 rule, formula, seed value, illustrative value or mark allocation.

## Impact

- **Size:** architectural. It renames the M1 starter's packages and public types, changes its HTTP contract (paths, port), changes the marking harness that other scripts depend on, and adds a new document kind to the PDF pipeline.
- **Files affected:** about 35.
- **Complexity:** medium.
- **Risk:** medium.
  - The marking harness and selfcheck must stay exact (starter ≤ 20, solution = 100).
  - Jackson must deserialise the request class.
  - The student's dirty tree must not be touched: work happens in a separate worktree, stacked on change 003's branch.
  - The student must not start M1 until this lands.

## Open Questions

1. **Resolved 2026-11-19 (student approved, panel skipped):** stack on `specclaw/003-d10-d11-exam-day-swagger` (PR not opened yet), so the exam-day guide and d10 agree. Merge 003 first, then 004.

---

**To proceed:** Review this proposal and approve to begin planning.
