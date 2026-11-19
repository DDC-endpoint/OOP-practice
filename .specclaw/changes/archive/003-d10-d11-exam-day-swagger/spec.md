# Spec: Align d10 and d11 with the official LBP exam-day setup

**Change:** 003-d10-d11-exam-day-swagger
**Size:** bounded
**Source:** `proposal.md`. The official instructions are in `D:\year-2-prep\OOP\05_LBP_Practice_Papers\OOP_LBP_00_Exam_Day_Instructions.md`.

## Functional requirements

- **FR-1:** d10 and d11 serve Swagger UI from springdoc `2.6.0` (`org.springdoc:springdoc-openapi-starter-webmvc-ui`), which works with the parent's Spring Boot 3.3.5. No other dependency, parent or Java version changes.
- **FR-2:** The d10 `LESSON.md` teaches the run-and-check loop with **Swagger UI as the primary tool**:
  - the URL `http://localhost:8080/swagger-ui/index.html`, with `/swagger-ui.html` as the fallback
  - expand the endpoint → **Try it out** → fill in the params or JSON body → **Execute** → read the code, then the body
  - curl/Postman kept only as a short fallback

  The task-list item, the HIT step, the worked example, "Your task", the hints and the viva answers stop telling the student to "curl" as the main check.
- **FR-3:** The d10 exam link names the official structure: Q1 Section A (Java/OOP), Q2 Section B (Spring Boot/REST), **Section C – Functional Verification**. It quotes the official text. The old practice-paper wording, where kept, is labelled as practice-paper wording (the practice papers' "Section B" is the real exam's Section C).
- **FR-4:** d10 has an **exam-day rules** box, quoted or paraphrased from the official instructions:
  - 120 min = 110 for coding and testing + the final 10 only for the ZIP and the Blackboard upload
  - JDK 25 and IntelliJ IDEA Ultimate
  - only the starter project open, one desktop
  - exactly ONE new Java class (Q1(a)), and the TODOs everywhere else
  - don't change the config, repository, request or response classes
  - don't add dependencies (the starter already has Swagger)
  - no notes, no AI tools, none of your own earlier code
  - a technical problem means raise your hand
- **FR-5:** The d10 ZIP checklist follows the 110 + 10 split. Checks finish before minute 110. The last 10 minutes are: stop the app, ZIP the whole project folder, check the ZIP's contents, upload to Blackboard. It no longer tells the student to extract and re-run the ZIP in that window.
- **FR-6:** The d11 `LESSON.md` exam link maps to the official sections. The P3 quotes stay, labelled as practice-paper wording. "Then try it for real" becomes a Swagger walkthrough of the same scenario (commit twice with the same body → same result, list → 6 bookings, change `end` → 409), with curl as the fallback.
- **FR-7:** Both lessons say once that the real exam runs JDK 25 (and probably a newer Spring Boot), while the drills run JDK 17/Boot 3.3.5, and that the code you write is the same.
- **FR-8:** The d10 and d11 `LESSON.pdf`, `HINTS.pdf` and `papers/d10.md`, `papers/d11.md` are regenerated from the edited Markdown.

## Non-functional requirements

- **NFR-1:** No change to any `src/` file, test, TODO or planted bug.
- **NFR-2:** The student's uncommitted working-tree changes (d01–d10 `src/main` files and the d10 `pom.xml`) survive untouched. No stash, reset or checkout of their paths, and only this change's files get committed.
- **NFR-3:** Lessons keep the pack's structure (front matter + idea, worked example, task, hints, viva), and hints stay inside `<details>` so the PDF leak checker passes.

## Acceptance criteria

- **AC-1:** Both poms declare springdoc `2.6.0`. On `main`, `d10` starts. Its tests fail only on the 3 planted bugs (`unknownId`, `quotesOrder`, `quoteMoreLongFilms`), with no `NoClassDefFoundError`/context failure.
- **AC-2:** On `solutions` (after merging), `d10` passes 10/10 and `d11` passes all of `BookingServiceTest` + `RoomBookingApiTest` (20).
- **AC-3:** With the app running, `GET /swagger-ui/index.html` answers 200 for d10 and d11, and `GET /v3/api-docs` lists their endpoints.
- **AC-4:** The d10 `LESSON.md` contains "Section C", "Swagger", "Try it out", "Execute", "110" and "Blackboard". curl appears only in a section or paragraph marked as the fallback.
- **AC-5:** The d11 `LESSON.md` contains a Swagger walkthrough with "Try it out" and the 409 step.
- **AC-6:** `bash scripts/build-pdfs.sh` succeeds, with no leak-check failure, and `scripts/sync-papers.sh` refreshes `papers/d10.md` and `papers/d11.md`.
- **AC-7:** `bash scripts/selfcheck.sh` passes.
- **AC-8:** `git diff --name-only main...HEAD` lists only the files in the Approach file map (plus `.specclaw/` bookkeeping).

## Edge cases

- **The student's d10 pom edit vs the committed pom.** The committed d10 `pom.xml` is written to match the working-tree content exactly, so `git status` afterwards shows it clean, with no conflict.
- **d11 under springdoc.** d11 has more DTOs, including records and nested lists. If springdoc fails to introspect them, AC-1/2 catch it before commit.

## Approach

**Decision:** keep Boot 3.3.5 / JDK 17 (the student's choice, 2026-11-19). Add springdoc 2.6.0 and rewrite only the run/check/submit prose. Commit on branch `specclaw/003-d10-d11-exam-day-swagger`, created from `main` with the working tree carried over. Stage paths explicitly. Then merge into `solutions` per `.specclaw/context.md` ("After **any** commit on the content branch, run `git switch solutions && git merge <branch>`").

**File map:**
- `drills/d10-debug-and-check/pom.xml`, `LESSON.md`, `LESSON.pdf`, `HINTS.pdf`
- `drills/d11-stateful-commit/pom.xml`, `LESSON.md`, `LESSON.pdf`, `HINTS.pdf`
- `papers/d10.md`, `papers/d11.md`
