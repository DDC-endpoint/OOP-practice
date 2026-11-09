# OOP-practice: LBP practice pack

Practice for the **5COSC019W OOP Lab-Based Practical**: 100 minutes in IntelliJ on a Spring Boot starter, Section A 18/22/20/20 + Section B 20, followed by a viva.

**Teach first, then test.** Drills each teach one skill with a lesson, then mark you with tests. Mock and past papers rehearse the real thing and give you a score.

## Set up on any device (once)

1. Install **JDK 17** and **IntelliJ IDEA** (Community is fine).
2. Clone:
   ```bash
   git clone https://github.com/DDC-endpoint/OOP-practice.git
   ```
3. IntelliJ: **File → Open →** the `OOP-practice` folder → *Trust project*. It imports as a Maven project with every drill inside.
4. **File → Project Structure → SDK → 17**.

The first build downloads Maven and Spring Boot automatically (needs internet, takes a few minutes). There's nothing else to install.

> While the pack is still being built, the newest drills are on the branch `specclaw/001-lbp-practice-pack`. After cloning, run `git switch specclaw/001-lbp-practice-pack`. Once it's merged, `main` has everything.

## Do a drill

1. Open `drills/dNN-…/LESSON.md` (IntelliJ shows it formatted). Read **The idea** and the **Worked example**.
2. Do **Your task** in `src/main/java/...`. Use the **Hints** only if you're stuck, and open them one at a time.
3. Run the tests: right-click `src/test/java` in that drill → **Run 'All Tests'**. Everything starts red, and you're done when it's all green.
4. Say the **Viva questions** out loud before opening the answers.

From a terminal instead: `./mvnw -pl drills/d01-classes-constructors test` (Windows: `mvnw.cmd -pl drills\d01-classes-constructors test`).

### Compare with the model solution (after you're green, or truly stuck)
```bash
git diff HEAD solutions -- drills/d01-classes-constructors
```
Don't switch to the `solutions` branch while you're working. Just diff against it.

## Drills

| Drill | Skill | Exam link |
|---|---|---|
| d01 | classes, `private final`, `super(...)`, validation | Task 1 |
| d02 | create a subclass from scratch, seed the repository | Task 2 |
| d03 | ceiling division, tiered price, overflow → `IllegalArgumentException` | Tasks 1–2 |
| d04–d11 | polymorphism, sorting, DTOs, validation, error handling, REST, debugging, stateful commit | *coming next* |

## Papers (coming next)
- `mock-papers/M1-courier`: a new mock in the exact LBP format.
- `past-papers/P1…P3`: the three official practice papers, with marking.
- Mark an attempt: `java marking/Mark.java p1 past-papers/P1-parking` → marks per task out of 100.
