# CLAUDE.md — OOP-practice

A practice pack for the 5COSC019W OOP Lab-Based Practical (LBP). The real exam (official instructions, 2026-11-19) is 120 minutes (110 + 10 for ZIP/upload) on JDK 25 + IntelliJ Ultimate, in the order Q1 Section A → Q2 Section B → Section C functional checks, with a viva afterwards. The pack runs Java 17 / Spring Boot 3.3.5. P1–P3 keep their original 100-minute A/B format; M1 follows the official format. Design: `docs/design.md`. The source materials are in `D:\year-2-prep\OOP\`.

## Session rules
- **Start of session:** read `HANDOFF.md` first.
- **End of session:** update `HANDOFF.md` before finishing. Overwrite "Current state", "Next steps" and "Open questions", and add a dated entry to "Session log".
- Work goes through SpecClaw (`.specclaw/`). Changes 001–004 are archived; start new work with `/specclaw:propose`. Read `.specclaw/context.md` for project rules.
- The student is still learning. Lessons teach first (concept → worked example → task → hints), in plain w3schools-like language.

## Hard rules
- **Solutions never go on `main`.** Model solutions live only on the `solutions` branch. `main` holds starters, lessons and tests.
- **Mock and past-paper marking tests stay outside the project's `src/`** (under `marking/`) and are applied by script after the attempt. Drill tests may live in the drill's `src/test`.
- New or reworked papers follow the module mock's conventions (see M1): packages `controller / exception / model / repository / service`, plain classes with getters (no records, no `dto`), `GlobalExceptionHandler` + `ErrorResponse`, `/resource` paths on port 8080, Swagger (springdoc 2.6.0). The older drills and P1–P3 keep `dto`/records/`ApiError`. See `.specclaw/context.md`.
- **The student's working tree holds their own uncommitted attempts.** Never commit them. Work in git worktrees and commit with explicit pathspecs.
- Tests must check real behaviour (values, status codes, ordering, error bodies) so hard-coded answers fail. Where a paper forbids something (for example `instanceof`), check it by scanning the source.
- Scenarios are new and fictional. Never claim to predict the real exam.
- Every paper and drill brief is mirrored in `papers/*.md` with front matter (`id, module, kind, title, difficulty, timeMinutes, tasks[{name, marks}]`) for the future study site.

## Build and test
- Use JDK 17: `C:\Program Files\Java\jdk-17`. The PATH default is JDK 21, so set `JAVA_HOME` for command-line builds.
- Maven isn't installed. Use the wrapper: `./mvnw test` (bash) or `mvnw.cmd test` (Windows).
- Functional checks (Section C in M1, Section B in P1–P3) run as `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`. The IntelliJ HTTP Client is Ultimate-only.

## Remote
- **Public** GitHub repo `OOP-practice` (friends fork it; the README has fork steps) (account `DDC-endpoint` (keyring label EnderGuardian25)), with branches `main` and `solutions`.
