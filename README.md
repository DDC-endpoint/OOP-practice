# OOP-practice: LBP practice pack

Practice for the **5COSC019W OOP Lab-Based Practical (LBP)**. It's 100 minutes in IntelliJ on a supplied Java 17 / Maven / Spring Boot starter, worth 40% of the module, and you need at least 30 to qualify. The marks are:
- **Section A:** four coding tasks worth 18, 22, 20 and 20
- **Section B:** six functional checks on your running app, worth 20

A **viva** follows, where you explain your code.

**Teach first, then test.**
- **Drills** teach one skill each with a lesson, then mark you with tests.
- **Papers** rehearse the real thing and give you a score out of 100.

## Set up on any device (once)

1. Install **JDK 17** and **IntelliJ IDEA** (Community is fine).
2. Clone:
   ```bash
   git clone https://github.com/DDC-endpoint/OOP-practice.git
   ```
3. In IntelliJ: **File → Open →** the `OOP-practice` folder → *Trust project*. All the drills open as one Maven project.
4. **File → Project Structure → SDK → 17**.

The first build downloads Maven and Spring Boot automatically, so it needs internet and takes a few minutes. You don't need to install Maven.

> Get updates any time with `git pull`. Stay on `main`; never work on the `solutions` branch.

## 1 · Do a drill

1. Open `drills/dNN-…/LESSON.md` (IntelliJ shows it formatted). Read **The idea** and the **Worked example**.
2. Do **Your task** in `src/main/java/...`. Open the **Hints** one at a time, and only when you're stuck.
3. Right-click the drill's `src/test/java` → **Run 'All Tests'**. It starts red. You're done when it's all green.
4. Answer the **Viva questions** out loud before you open the answers.

Terminal alternative: `./mvnw -pl drills/d01-classes-constructors test` (Windows: `mvnw.cmd -pl drills\d01-classes-constructors test`).

| Drill | Skill | Exam link |
|---|---|---|
| d01 | classes, `private final`, `super(...)`, validation | Task 1 |
| d02 | create a subclass from scratch, seed the repository | Task 2 |
| d03 | ceiling division, tiered price, overflow → `IllegalArgumentException` | Tasks 1–2 |
| d04 | polymorphism over `List<Parent>`, no `instanceof` | Task 2 |
| d05 | `Comparator.comparing(...).thenComparing(...)`, streams | Task 2 |
| d06 | records as DTOs, mapping model → DTO | Tasks 2–3 |
| d07 | service validation: positive, blank, `trim`, unique, not-found | Task 3 |
| d08 | `@RestControllerAdvice` → 400 / 404 / 409 with `ApiError` | Task 3 |
| d09 | `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, 201 | Task 4 |
| d10 | run the app, curl it, read stack traces, fix bugs, ZIP checklist | Section B |
| d11 | *stretch:* preview vs commit, idempotent retry, half-open intervals | P2/P3 level |

## 2 · Sit a paper (exam conditions)

| Paper | Folder | Level |
|---|---|---|
| P1 Parking | `past-papers/P1-parking` | official practice, simple |
| P2 Drone dispatch | `past-papers/P2-drone` | official practice, moderate |
| P3 Scheduler | `past-papers/P3-scheduler` | official practice, extreme (stretch) |
| **M1 Courier** | `mock-papers/M1-courier` | **new mock in the exact LBP format** |

1. In IntelliJ: **File → Open →** the paper's folder (a separate window, like the real exam). Read `PAPER.md`.
2. Set a **100-minute timer**. Use no hints and no solutions. Keep the app compiling. Leave the last 10 minutes for running checks and zipping.
3. Mark it from the repo root:
   ```bash
   java marking/Mark.java p1 past-papers/P1-parking        # m1 | p1 | p2 | p3
   ```
   On Windows you can also run `marking\mark.cmd p1 past-papers\P1-parking`. You'll get marks per task, out of 100. The score is an estimate built from the paper's stated rules. Staff marking and the viva can differ.
4. Find the lowest task, then redo the drill for it.

**Starting a paper again from scratch.** Your attempt gets overwritten, so first zip your attempt somewhere else (good exam practice anyway). Then:
```bash
git restore past-papers/P1-parking && git clean -fd past-papers/P1-parking
```

## 3 · Compare with the model solution

Do this after you're green or have finished a paper, or when you're truly stuck:
```bash
git diff HEAD solutions -- drills/d04-polymorphism
git diff HEAD solutions -- past-papers/P1-parking
```
Don't switch to the `solutions` branch while you work. Just diff against it.

## 4 · Read on a tablet or on paper (PDFs)

Every drill folder also has two PDFs next to `LESSON.md`:
- **`LESSON.pdf`**: the idea, worked example, task and viva questions. No hints and no answers, so nothing is spoiled while you work.
- **`HINTS.pdf`**: the hints, numbered in order, then the viva answers. Open it only when you're stuck, one hint at a time.

Each paper folder has a **`PAPER.pdf`** next to `PAPER.md`.

The Markdown is the source. If you edit a lesson or paper, rebuild the PDFs. This needs Node 18+ and Chrome or Edge. The first run installs its packages into `scripts/pdf/`:
```bash
bash scripts/build-pdfs.sh            # Windows: scripts\build-pdfs.cmd
bash scripts/build-pdfs.sh d04        # just one folder
```
It also checks that no hint or answer has leaked into a `LESSON.pdf`.

## Plan up to the LBP (Week 9 seminar)

| Day | Do | Time |
|---|---|---|
| Mon 9 Nov | d01, d02, d03 | ~1 h |
| Tue 10 Nov | d04, d05, d06 | ~1 h |
| Wed 11 Nov | d07, d08, d09. Week 8 also has the tutors' **mock LBP** in your tutorial | ~1.5 h |
| Thu 12 Nov | d10, then **P1 timed** → mark → redo the weakest drill | ~2.5 h |
| Fri 13 Nov | **P2 timed** → mark → review | ~2.5 h |
| Sat 14 Nov | **M1 timed, full exam conditions** → mark → viva questions out loud | ~2.5 h |
| Sun 15 Nov | Redo your weakest paper from a fresh starter. Optional: d11, P3 Tasks 1, 2 and 4 | ~2 h |
| Day before | No new material. Type the patterns from memory (controller, handler, comparator, ceiling division), and run through the viva questions | ~1 h |

## What's in here

```
drills/          11 one-skill drills (one Maven project, open the repo root)
past-papers/     P1–P3 official practice starters + PAPER.md
mock-papers/     M1 new mock + PAPER.md + requests.http
marking/         Mark.java + marking tests for each paper (don't open before sitting it!)
papers/          every lesson and paper as Markdown with front matter (for the study site)
scripts/         selfcheck.sh (checks the whole pack), sync-papers.sh, build-pdfs.sh/.cmd (the PDFs)
```
