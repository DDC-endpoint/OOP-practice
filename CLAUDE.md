# CLAUDE.md — OOP-practice

A practice pack for the 5COSC019W OOP Lab-Based Practical (LBP): a 100-minute timed task in IntelliJ on a Java 17 / Maven / Spring Boot starter. Section A has four coding tasks (18/22/20/20) and Section B has six functional checks (20), with a viva afterwards. Design: `docs/design.md`. The source materials are in `D:\year-2-prep\OOP\`.

## Session rules
- **Start of session:** read `HANDOFF.md` first.
- **End of session:** update `HANDOFF.md` before finishing. Overwrite "Current state", "Next steps" and "Open questions", and add a dated entry to "Session log".
- Work goes through SpecClaw (`.specclaw/`). The current change is `001-lbp-practice-pack`.
- The student is still learning. Lessons teach first (concept → worked example → task → hints), in plain w3schools-like language.

## Hard rules
- **Solutions never go on `main`.** Model solutions live only on the `solutions` branch. `main` holds starters, lessons and tests.
- **Mock and past-paper marking tests stay outside the project's `src/`** (under `marking/`) and are applied by script after the attempt. Drill tests may live in the drill's `src/test`.
- Match the LBP starters exactly: Java 17, Spring Boot 3.3.5, packages `model / dto / repo / service / controller / error`, records for DTOs, an in-memory `ConcurrentHashMap` repository, `ApiError(code, message)`.
- Tests must check real behaviour (values, status codes, ordering, error bodies) so hard-coded answers fail. Where a paper forbids something (for example `instanceof`), check it by scanning the source.
- Scenarios are new and fictional. Never claim to predict the real exam.
- Every paper and drill brief is mirrored in `papers/*.md` with front matter (`id, module, kind, title, difficulty, timeMinutes, tasks[{name, marks}]`) for the future study site.

## Build and test
- Use JDK 17: `C:\Program Files\Java\jdk-17`. The PATH default is JDK 21, so set `JAVA_HOME` for command-line builds.
- Maven isn't installed. Use the wrapper: `./mvnw test` (bash) or `mvnw.cmd test` (Windows).
- Section B checks run as `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`. The IntelliJ HTTP Client is Ultimate-only.

## Remote
- Private GitHub repo `OOP-practice` (account `EnderGuardian25`), with branches `main` and `solutions`.
