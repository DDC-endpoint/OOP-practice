# Design: 004-m1-exam-conventions-and-guides

## Approach
- **Work location:** a separate worktree on branch `specclaw/004-...`, stacked on `specclaw/003-...`. The student's main tree, which is on 003 and holds uncommitted drill work, isn't touched until the end, when it switches to 004 (no conflict, because M1 isn't dirty there).
- **M1:** a mechanical rename (`git mv`) plus record → class rewrites. TODO comments are reworded only where a name changed. The starter stays unsolved.
- **Solutions:** a separate `solutions` worktree. Merge 004 in, then port the six solved files (`ParcelController`, `GlobalExceptionHandler`, `ExpressParcel`, `StandardParcel`, `ParcelRepository`, `ParcelService`) to the new names and getters. That's the only `src/main` diff on `solutions` (context.md: "`solutions` may differ only under `src/main/`").
- **Marking:** edit the imports, URLs and accessors in `marking/m1/*`, and rename `SectionBTest` → `SectionCTest`. `Mark.java` gets a per-paper allocation: m1 uses `SectionCTest`, p1–p3 `SectionBTest`.
- **PDF pipeline:** `lib.mjs#sources()` also yields `{kind:'guide', dir:'guides', src:'guides/<NAME>.md'}`. `build.mjs` renders it with `paperHtml` to `guides/<NAME>.pdf`. `check.mjs` treats a guide like a paper (all `## ` headings in order) but with its own PDF name.

## File map
- `mock-papers/M1-courier/{pom.xml, PAPER.md, PAPER.pdf, README.md, requests.http, src/main/resources/application.properties, src/main/java/edu/practice/courier/**}`
- `marking/m1/src/test/java/marking/*.java`, `marking/Mark.java`
- `papers/m1.md`
- `guides/EXAM-DAY.md`, `guides/EXAM-DAY.pdf`, `guides/REFRESHER.md`, `guides/REFRESHER.pdf`
- `scripts/pdf/lib.mjs`, `scripts/pdf/build.mjs`, `scripts/pdf/check.mjs`
- `README.md`
- `scripts/selfcheck.sh` (only if it hard-codes `SectionBTest` or the M1 paths)

## Risks
- The marking totals assertion must keep 100 for m1.
- The Swagger example body must be correct for the request class.
