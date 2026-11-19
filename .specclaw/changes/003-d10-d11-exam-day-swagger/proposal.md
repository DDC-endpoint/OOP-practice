# Proposal: Align d10 and d11 with the official LBP exam-day setup (Swagger UI, Section C, 120 min)

**Created:** 2026-11-19
**Status:** 🟡 Draft

## Problem

On 2026-11-19, the day before the LBP, the student received the official exam-day instructions (`D:\year-2-prep\OOP\05_LBP_Practice_Papers\OOP_LBP_00_Exam_Day_Instructions.md`). In several places they contradict what d10 and d11 teach:

- **Testing tool.** *"The provided starter project includes an integrated Swagger UI interface for testing and verifying REST API endpoints."* d10's whole "run and check" loop, and d11's "try it for real", teach curl (with PowerShell `curl.exe` quoting traps) and Postman. Neither drill project has Swagger, so the student can't practise the tool they'll actually use. The student tried adding springdoc `2.8.6` to d10 by hand. It needs Boot 3.4+, the pack is on Boot 3.3.5, and the app stopped starting (`NoClassDefFoundError: LiteWebJarsResourceResolver`), so all 10 tests errored.
- **Section names.** The real exam is Q1 Section A (Java/OOP) → Q2 Section B (Spring Boot/REST) → **Section C: Functional Verification** (*"Run the application and verify the required API endpoints … using the permitted testing interface"*). d10 calls the running-app check "Section B (20 marks)" and quotes the older practice briefing.
- **Timing.** The exam is 120 minutes: the first 110 to read, code and test, then the *"Final 10 minutes: Creating a ZIP file of your completed project and uploading it to Blackboard"*. d10's ZIP checklist quotes the older briefing's "final 10 minutes for running final checks, verifying the ZIP", and has the student extract and re-run the ZIP inside that window.
- **Environment rules.** JDK 25 + IntelliJ Ultimate only. Only the starter project may be open. Exactly ONE new Java class. No personal notes, AI tools, or earlier code of your own. d10 has none of this.

## Proposed Solution

Keep the drills' code tasks and their planted bugs exactly as they are. Change only how the student runs, checks and submits, so it matches the exam.

1. **Swagger in both drills.** Add `springdoc-openapi-starter-webmvc-ui` **2.6.0** to `drills/d10-debug-and-check/pom.xml` and `drills/d11-stateful-commit/pom.xml`. That's the springdoc line built for Spring Boot 3.3.x, and it was checked on 2026-11-19: d10's app starts and its 7 non-bug tests pass. The d10 pom entry should match the student's working-tree edit byte for byte, so their checkout doesn't conflict.
2. **d10 `LESSON.md`:**
   - Retitle the exam link to **Section C – Functional Verification** and quote the official instructions.
   - Make **Swagger UI** the primary HIT step: `http://localhost:8080/swagger-ui/index.html`, then expand the endpoint, **Try it out**, fill in the params or JSON body, **Execute**, and read the code and the body. Keep curl as a short fallback.
   - Replace the ZIP checklist with the official 110 + 10 split: finish checks before minute 110, then ZIP and upload in the last 10.
   - Add a short "Exam-day rules" box: JDK 25, IntelliJ Ultimate, one project open, exactly one new class, don't add dependencies (the starter already has Swagger), no notes, no AI.
   - Update the task line, the hints and the viva answers that say "curl it".
3. **d11 `LESSON.md`:** change the exam link to the official Q1/Q2/Section C names (the P3 quotes stay, marked as practice-paper wording). Rewrite "Then try it for real" as a Swagger walkthrough, with curl as the fallback.
4. **Rebuild the d10 and d11 `LESSON.pdf` and `HINTS.pdf`** with `scripts/build-pdfs.sh`. Its leak checker must pass.
5. **`solutions` branch:** merge `main` in, then confirm that d10 and d11 pass all their tests with springdoc added (10 for d10, 12 + 8 for d11).

## Scope

### In Scope
- `drills/d10-debug-and-check/{pom.xml, LESSON.md, LESSON.pdf, HINTS.pdf}`
- `drills/d11-stateful-commit/{pom.xml, LESSON.md, LESSON.pdf, HINTS.pdf}`
- Merging `main` into `solutions` and running the tests there.

### Out of Scope
- Moving the pack to Spring Boot 4.1.1 / JDK 25. The parent pom is shared by all 11 drills and 4 papers, JDK 25 isn't installed on this PC, and the LBP is tomorrow. The lessons will instead *say* that the exam runs JDK 25 and that the code they write is the same.
- d01–d09, P1–P3, M1 and `papers/*.md`.
- Any change to the drill code, the TODOs, the planted bugs or the tests.
- The student's uncommitted drill work. It must survive the branch switch untouched.

## Impact

- **Size:** bounded. It changes two existing drills' docs and poms, and adds no new flow or interface.
- **Files affected:** 8 on `main`, plus the merge into `solutions`.
- **Complexity:** small.
- **Risk:** low to medium. The risks are a dependency breaking app startup (mitigated: 2.6.0 has been run on d10 already, and d11 and `solutions` will be test-run), and clobbering the student's uncommitted work during the branch switch. The fix for that is to branch from the current working tree without stash/reset, and to commit only the 8 named paths.

## Open Questions

1. ~~Stack~~ **Resolved 2026-11-19:** the student chose to stay on Boot 3.3.5 + JDK 17 with springdoc 2.6.0. The party panel was skipped at the student's request.
2. Is the Swagger path in the real starter `/swagger-ui/index.html`? That's the springdoc default. The lesson will also mention `/swagger-ui.html` as the fallback.

---

**To proceed:** Review this proposal and approve to begin planning.
