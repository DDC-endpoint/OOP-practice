---
id: d10
module: OOP
kind: drill
title: Debug and check a running API
difficulty: 2
timeMinutes: 20
tasks:
  - { name: "Fix: an unknown id answers 404 NOT_FOUND, not 500", marks: 3 }
  - { name: "Fix: the long-film surcharge matches the paper's values", marks: 3 }
  - { name: "Fix: the quotes list order (total DESC, then id ASC)", marks: 3 }
  - { name: "Run the app, check it in Swagger UI and compare with the paper", marks: 1 }
---

# d10 · Debug and check a running API

> **Exam link:** this is LBP **Section C – Functional Verification**. The official instructions: *"Run the application and verify the required API endpoints and expected behaviours using the permitted testing interface"*, and *"The provided starter project includes an integrated Swagger UI interface for testing and verifying REST API endpoints."* The exam order is **Q1 Section A** (Java/OOP) → **Q2 Section B** (Spring Boot/REST) → **Section C** (run it and check it).
>
> The practice papers (P1–P3, M1) still use the older names, where this part is called "Section B" (20 marks). Their wording is still a good guide: *"A project that does not start cannot demonstrate the Section B behaviours"*, *"Correct compilation is necessary but does not by itself prove correct functionality"*, and checks like *"A missing pass quotation returns 404 and the correct error code"*.

> **Exam-day rules** (from the official instructions, 2026-11-19):
> - **120 minutes:** the first **110** to read, code and test, then the **final 10 only** to ZIP the project and upload it to **Blackboard**. The timer can't be paused, and you get one attempt.
> - **JDK 25** and **IntelliJ IDEA Ultimate** only. The drills here run JDK 17 / Spring Boot 3.3, but the code you write is the same.
> - Only the **starter project** may be open, one project, one desktop. No virtual desktops.
> - Create **exactly ONE new Java class** (Question 1(a)). Everything else goes under the existing `// TODO`s. Don't change the supplied config, repository, request or response classes unless the paper says so.
> - **Don't add dependencies.** The starter already has Swagger UI. A wrong version stops the app from starting, and then Section C scores nothing.
> - **Allowed:** lecture slides and Blackboard materials. **Not allowed:** personal notes, AI tools (that includes IntelliJ AI Assistant and Copilot), messaging apps, other people's code, or your own earlier code.
> - IntelliJ, JDK, Maven or Blackboard problem? **Raise your hand.** Don't close IntelliJ.

## The idea

In Section C the examiner **runs** your app and sends it requests. Code that compiles but answers 500, or gives 1300 where the paper says 1150, scores nothing for that check. So before you submit, you check your own app the same way, in a loop:

```
1. RUN      start the app
2. HIT      send the request from the paper (Swagger UI: Try it out, Execute)
3. COMPARE  what you got  vs  what the paper's illustrative values say
4. READ     if it crashed, read the stack trace down to the first edu.practice line
5. FIX      change the smallest thing
6. AGAIN    restart the app, send the same request
```

### 1. Run

Open the class with `@SpringBootApplication` (here `CinemaApplication`). Click the **green triangle** next to `public class` or `main`. Wait for this line in the Run window:

```
Started CinemaApplication in 2.3 seconds (process running for 2.8)
```

Now the API is live at `http://localhost:8080`.

- **The running app does not see your edits.** After every change, press the green **Rerun** arrow (Ctrl+F5) in the Run window.
- `Port 8080 was already in use` means an older copy is still running. Stop it with the red square, then run again.
- The tests (`CinemaApiTest`) start their own copy on a random port, so they never clash with the app you started.

### 2. Hit the endpoints: Swagger UI

With the app running, open this in the browser:

```
http://localhost:8080/swagger-ui/index.html
```

The port is whatever the app printed (`Tomcat started on port ...`), set by `server.port` in `application.properties`. The exam starters may use 8081, 8082 and so on. If that page gives 404, try `/swagger-ui.html`.

Swagger lists every endpoint of every controller, grouped by controller. For each request from the paper:

1. Click the endpoint (for example `GET /api/screenings/{id}/quote`) to expand it.
2. Click **Try it out**.
3. Fill in the boxes. Each `@PathVariable` and `@RequestParam` gets its own box (`id` = 3, `seats` = 2). For a POST, replace the example JSON in **Request body** with the paper's JSON.
4. Click **Execute**.
5. Scroll down to **Server response**. Read the **Code** first (200, 201, 400, 404 …), then the **Response body**.

**Trap:** below Server response there's a second table called **Responses**. That's only the list of codes Swagger *expects* the endpoint could return. It is **not** what your app answered. Always read **Server response → Code**.

Swagger also shows the **Request URL** it called, for example `http://localhost:8080/api/screenings/3/quote?seats=2`. Compare it with the paper's URL when something answers 404 unexpectedly, because that usually means the mapping path is wrong.

**Fallback: curl**, only if Swagger won't load. In **Git Bash** (or macOS/Linux):

```bash
curl -i http://localhost:8080/api/screenings/3
curl -i "http://localhost:8080/api/screenings/3/quote?seats=2"
curl -i -X POST http://localhost:8080/api/screenings -H "Content-Type: application/json" \
     -d '{"id":7,"film":"Quiet Storm","runtimeMinutes":133,"basePriceCents":1000}'
```

**Windows PowerShell:** type `curl.exe`, not `curl`. Put the JSON in a file and send it with `--data "@new-screening.json"`, keeping it in quotes. `-i` prints the status line. Without `-H "Content-Type: application/json"` Spring answers **415**. Put quotes around any URL that has `?` or `&` in it.

### 3. Compare

Make a tiny table from the paper's illustrative values, and fill it in as you go:

| Request | Paper says | I got | |
|---|---|---|---|
| `GET /api/screenings/3/quote?seats=2` | 200, `totalCents` 2200 | 200, 2200 | ✔ |
| `GET /api/screenings/99` | 404, `NOT_FOUND` | 500 | ✘ |

Check the **status** first, then each field in the body, then the **order** of any list. After the paper's own values, try one **edge** of your own: an exact boundary (60 minutes, 120 minutes), the smallest and the largest allowed value, and one invalid value.

### 4. Read the stack trace

When the answer is a **500**, an exception escaped. IntelliJ's Run window shows the stack trace. Read it **from the top**:

```
ERROR ... Servlet.service() ... threw exception [Request processing failed:
      java.lang.ArithmeticException: / by zero] with root cause

java.lang.ArithmeticException: / by zero                          <- 1. WHAT went wrong
	at edu.practice.shop.service.CartService.average(CartService.java:41)     <- 2. WHERE: first edu.practice line
	at edu.practice.shop.controller.CartController.average(CartController.java:28)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(...)
	... 60 more lines of org.springframework / org.apache ...     <- skip all of these
```

1. The first line says **what**: the exception class and its message.
2. Go down to the **first line that starts with `at edu.practice`**. That's your code. Click the blue `CartService.java:41` link and IntelliJ jumps to the line.
3. Skip the `java.base`, `org.springframework` and `org.apache` lines. That's plumbing.
4. If there's a `Caused by:`, the **last** one is the real cause.

Then ask one question: **did we throw this exception on purpose?**

| The exception is… | It means… | Look at… |
|---|---|---|
| a Java one: `NullPointerException`, `ArithmeticException`, `UnsupportedOperationException` (a TODO) | the code at that line is wrong | the line in the trace |
| **our own**: `IllegalArgumentException("...")`, `XNotFoundException`, `XConflictException` | the throw is **correct**; nobody turned it into a status code | `ApiExceptionHandler` (d08) |

No stack trace at all, but a **wrong number or wrong order**? Then nothing crashed: the logic is wrong. Follow the call from the controller method, to the service method, to the model method that computes that value.

### 5. Fix the smallest thing

Change **one** thing, the smallest change that could explain the symptom. Don't rewrite a method that mostly works, and never change the rules or the illustrative values to match your output. Then go back to step 6: restart, send the same request, and also re-check a value that was already right, so the fix didn't break it.

### Status codes at a glance

| Status | Means | In the LBP |
|---|---|---|
| 200 | OK | GET, and any POST that doesn't create something |
| 201 | Created | a POST that adds a record |
| 400 | Bad Request | invalid input (`IllegalArgumentException` → `BAD_REQUEST`) |
| 404 | Not Found | unknown id (`XNotFoundException` → `NOT_FOUND`). Also what Spring answers when **no endpoint** matches the URL. |
| 409 | Conflict | valid, but clashes with stored state (`CONFLICT`) |
| 415 | Unsupported Media Type | the body wasn't sent as JSON (Swagger sets this for you; with curl, add `-H "Content-Type: application/json"`) |
| 500 | Internal Server Error | an exception escaped: a bug, or a missing handler |

### The ZIP checklist (the last 10 minutes)

The official instructions: *"First 110 minutes: … coding and testing your solution. Final 10 minutes: Creating a ZIP file of your completed project and uploading it to Blackboard."* So finish all your Swagger checks **before minute 110**. The last 10 minutes are only for the ZIP and the upload. A perfect project that is zipped wrongly, or never uploaded, scores nothing for Section C.

1. **At minute 110, stop coding.** Save, then stop the running app (red square).
2. Find the **project folder**: the folder that has `pom.xml` and `src` directly inside it (in IntelliJ: right-click the project's top folder → *Open In → Explorer*).
3. Zip the **whole folder**, not the `.java` files. In Explorer, right-click the folder → *Compress to ZIP file* (Windows 11), or *Send to → Compressed (zipped) folder*.
4. Double-click the ZIP and check that it has `pom.xml`, `src/main/java/...` and `src/main/resources/application.properties` inside. `target/` and `.idea/` don't matter.
5. **Upload the ZIP to Blackboard**, and wait until it confirms the submission.
6. Any problem with the upload: **raise your hand** straight away.

## Worked example

This one is a different app: a **bike hire API**. Its paper says:

> `GET /api/bikes/{id}/quote?hours=2&member=true`: price = `hours × rateCents`; members get 10% off. Unknown id → 404 `NOT_FOUND`. *Illustrative: bike 1 (500 cents/hour), 2 hours, member → 900. Bike 9 doesn't exist → 404.*

**Bug 1: a 500.** Run the app and open Swagger UI. Expand `GET /api/bikes/{id}/quote` → **Try it out**, then fill in `id` 9, `hours` 2, `member` false → **Execute**. Server response:

```
Code: 500
Response body:
{"timestamp":"...","status":500,"error":"Internal Server Error","path":"/api/bikes/9/quote"}
```

The paper says 404. The Run window shows:

```
java.lang.NullPointerException: Cannot invoke "edu.practice.bikes.model.Bike.getRateCents()" because "bike" is null
	at edu.practice.bikes.service.HireService.quote(HireService.java:27)
	at edu.practice.bikes.controller.BikeController.quote(BikeController.java:33)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	...
```

`NullPointerException` is a **Java** exception, so the bug is at the line. `HireService.java:27`:

```java
Bike bike = repo.get(id);                      // 26: null when the id doesn't exist
int price = hours * bike.getRateCents();       // 27: crashes on null
```

The smallest fix is the missing not-found check. The handler already maps `BikeNotFoundException` to 404:

```java
Bike bike = repo.get(id);
if (bike == null) throw new BikeNotFoundException(id);    // added
int price = hours * bike.getRateCents();
```

Rerun, and click **Execute** again with the same values: Code `404` and `{"code":"NOT_FOUND","message":"Bike not found: 9"}`. ✔ Re-check bike 1 still answers 200.

**Bug 2: a wrong number, no crash.**

| Request | Paper says | I got | |
|---|---|---|---|
| `/api/bikes/1/quote?hours=2&member=false` | 1000 | 1000 | ✔ |
| `/api/bikes/1/quote?hours=2&member=true` | 900 | 1000 | ✘ |

No stack trace, so the logic is wrong. Only the member price is wrong, so the discount line is the suspect:

```java
price = price - price * (DISCOUNT_PERCENT / 100);   // 10 / 100 is 0 in int maths, so nothing is taken off
```

Fix it by multiplying **before** dividing: `price = price - price * DISCOUNT_PERCENT / 100;`. Rerun: 900. ✔ Then a value of your own: 3 hours as a member → 1500 − 150 = **1350**. ✔

## Your task

The cinema API in `src/main/java/edu/practice/d10` is **finished, but it has exactly 3 bugs**. Each fix is a small change on one line. Find them by running and checking the app, not by guessing. Here is its "paper":

> **Cinema seat pricing.** Six screenings are seeded:
>
> | id | film | runtimeMinutes | basePriceCents |
> |---|---|---|---|
> | 1 | The Last Lighthouse | 95 | 900 |
> | 2 | Orbit of Ash | 150 | 1000 |
> | 3 | Paper Kingdoms | 128 | 950 |
> | 4 | Small Hours | 104 | 1100 |
> | 5 | Midnight Ferry | 200 | 1000 |
> | 6 | Glass Harbour | 180 | 400 |
>
> **Rules.**
> - Seat price = `basePriceCents` + **150 cents per surcharge block**. A film of 120 minutes or less has no surcharge. Beyond 120 minutes, **every started 30 minutes** is one block.
> - A quote is for 1 to 10 seats: `totalCents = seats × seat price`. Any other seat count → 400 `BAD_REQUEST`.
> - An unknown screening id → 404 `NOT_FOUND`, message `Screening not found: <id>`.
> - The quotes list is **most expensive first**; equal totals by **lowest id first**.
> - POST adds a screening and answers **201**. Invalid input → 400 `BAD_REQUEST`, nothing stored.
>
> **REST operations.**
> - `GET /api/screenings` and `GET /api/screenings/{id}`
> - `GET /api/screenings/{id}/quote?seats=2`
> - `GET /api/screenings/quotes?seats=2` (`seats` is optional, default 1)
> - `POST /api/screenings` with JSON `{"id":7,"film":"Quiet Storm","runtimeMinutes":133,"basePriceCents":1000}`
>
> **Illustrative values.**
> - Small Hours (104 min), 1 seat → 1100.
> - Paper Kingdoms (128 min: 8 over → 1 block), 2 seats → seat 1100, total **2200**.
> - Orbit of Ash (150 min: 30 over → 1 block) → seat **1150**.
> - `GET /api/screenings/quotes?seats=2` → screening ids **5, 2, 3, 4, 1, 6** (3 and 4 both cost 2200, so 3 comes first).
> - `GET /api/screenings/99` → **404** `{"code":"NOT_FOUND","message":"Screening not found: 99"}`.

1. Run `CinemaApiTest`. **7 checks pass**: that's what working looks like. **3 fail**, one per bug. Read each red message as a *symptom*.
2. Run `CinemaApplication` and open `http://localhost:8080/swagger-ui/index.html`. Reproduce each symptom in Swagger (**Try it out → Execute**) and fill in a compare table using the illustrative values above.
3. For each symptom, find the cause and fix the smallest thing. Don't edit the tests, the rules or the seed data.
4. Rerun until all **10 checks** are green. Then restart the app and click through every endpoint in Swagger again, including one invalid case each (`seats=0`, id 99, a POST with an empty `film`).
5. Practise the ZIP checklist, timed at 10 minutes, on `past-papers/P1-parking`. Don't practise it on this drill: drill folders share the parent `pom.xml` at the repo root, so a drill folder on its own won't open. Exam starters are standalone, like P1.

## Hints

<details><summary>Hint 1: which bug is which?</summary>

Sort the three red checks by the **kind** of symptom:

- a **500** where the paper wants 404: an exception escaped. Read the stack trace (step 4 of the loop).
- a **wrong number**: nothing crashed, so the maths is wrong. Follow quote → service → model.
- a **wrong order**: the values are right but in the wrong places. Look for a `Comparator`.

Fix one at a time, and rerun the tests after each fix. One more check should go green each time.
</details>

<details><summary>Hint 2: check 3 (the 500)</summary>

In Swagger, execute `GET /api/screenings/{id}` with `id` 99. The Code is 500. Now look at IntelliJ's Run window:

```
edu.practice.d10.error.ScreeningNotFoundException: Screening not found: 99
	at edu.practice.d10.service.ScreeningService.get(ScreeningService.java:31)
	at edu.practice.d10.controller.ScreeningController.get(ScreeningController.java:30)
	...
```

The first `edu.practice` line is `ScreeningService.java:31`, `throw new ScreeningNotFoundException(id);`. That throw is **on purpose** and correct. So why did nobody turn it into a 404? Open `error/ApiExceptionHandler.java`, and compare its two methods line by line. What does the 400 method have that the 404 method doesn't? (IntelliJ may also show the 404 method's name in grey: "never used".)
</details>

<details><summary>Hint 3: check 6 (the surcharge)</summary>

Make a table from the tests' messages and the rule:

| runtime | minutes over 120 | blocks expected | seat price you got |
|---|---|---|---|
| 128 | 8 | 1 | ✔ |
| 150 | 30 | 1 | ✘ one block too many |
| 151 | 31 | 2 | ✔ |
| 180 | 60 | 2 | ✘ one block too many |
| 200 | 80 | 3 | ✔ |

What do 30 and 60 have in common? Open `model/Screening.java`, `surchargeBlocks()`, and work its last line out by hand with `over = 30`. Then compare it with the ceiling formula from d03: `(a - 1) / b + 1`.
</details>

<details><summary>Hint 4: check 8 (the order)</summary>

Expected `[5, 2, 3, 4, 1, 6]`, got `[5, 2, 4, 3, 1, 6]`. Most expensive first is right. Only the **tie** (3 and 4 both cost the same) is the wrong way round, so the tie-break runs high id → low id. Open `service/ScreeningService.java`, `quotes(...)`, and read the `Comparator` chain. Remember d05's trap: what does a `.reversed()` at the **end** of a chain flip?
</details>

<details><summary>Hint 5: the three fixes (nearly the answer)</summary>

- `ApiExceptionHandler`: the 404 method is missing its annotation. Put `@ExceptionHandler(ScreeningNotFoundException.class)` on the line above `public ResponseEntity<ApiError> notFound(...)`.
- `Screening.surchargeBlocks()`: `over / BLOCK_MINUTES + 1` adds a block too many when `over` is an exact multiple of 30. Use d03's formula: `return (over - 1) / BLOCK_MINUTES + 1;`
- `ScreeningService.quotes(...)`: reverse only the first key: `Comparator.comparingInt(Quote::totalCents).reversed().thenComparingLong(Quote::screeningId)`
</details>

## Viva questions

<details><summary>Your API answers 500 for an unknown id. What does that tell you, and where do you look?</summary>

An exception escaped and nothing turned it into a status code. I read the stack trace from the top down to the first `edu.practice` line. If it's our own exception, like `ScreeningNotFoundException`, the throw is correct, and the `ApiExceptionHandler` is missing an `@ExceptionHandler` for it. If it's a Java exception, like a `NullPointerException`, the bug is at that line.
</details>

<details><summary>Why is <code>minutes / 30 + 1</code> wrong for "every started 30 minutes"?</summary>

It's right for 31–59 but wrong at exact multiples: for 30 minutes it gives 2 instead of 1. The integer ceiling formula `(minutes - 1) / 30 + 1` is right for every positive value. I always test the exact boundary (30, 60) as well as one value either side.
</details>

<details><summary>What does <code>.reversed()</code> do at the end of a Comparator chain?</summary>

It flips the whole chain built so far, including the tie-breaks. To sort the total high → low but ties by id low → high, I call `.reversed()` straight after the first key, and only then add `.thenComparingLong(...)`.
</details>

<details><summary>It compiles and the tests I ran pass. Why still run it and check it in Swagger?</summary>

Compiling doesn't prove the behaviour. Section C runs the app and checks status codes, values and order, so I check them the same way in Swagger UI: the paper's illustrative values first, then an edge case. I also restart the app after every change, because a running app keeps the old code.
</details>

<details><summary>What goes in the submission ZIP, and how do you check it?</summary>

The whole Maven project folder: `pom.xml` and `src` (main, resources and any tests), not just the `.java` files. I stop coding at minute 110, zip the folder, open the ZIP to check `pom.xml` and `src` are inside, and upload it to Blackboard in the final 10 minutes.
</details>
