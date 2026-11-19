---
id: exam-day
title: LBP exam-day guide
---

# LBP exam-day guide

Everything for the day of the OOP Lab-Based Practical, in the order you need it. It's built from the **official exam-day instructions** and the module team's **MODERATE mock paper** (Emergency Dispatch). Where the official instructions say something, they are quoted. They always win over the practice papers.

## At a glance

| | |
|---|---|
| **Length** | **120 minutes.** *"First 110 minutes: Reading and understanding the requirements, implementing the programming logic, coding and testing your solution. Final 10 minutes: Creating a ZIP file of your completed project and uploading it to Blackboard."* |
| **Timer** | *"The assessment timer starts when you open the assessment. It cannot be paused or restarted, and only one attempt is permitted."* |
| **Tools** | **JDK 25** + **IntelliJ IDEA Ultimate**. *"IntelliJ IDEA Ultimate is the only permitted IDE."* |
| **Order** | **Q1 Section A** (Java/OOP) → **Q2 Section B** (Spring Boot/REST) → **Section C** (functional verification). *"Complete the questions in sequence."* |
| **Testing** | *"The provided starter project includes an integrated Swagger UI interface for testing and verifying REST API endpoints."* |
| **New classes** | *"You must create exactly ONE new Java class, as specified in Question 1(a)."* |
| **Submit** | One ZIP of the **whole Maven project folder**, uploaded to **Blackboard**. |

What each section covers (from the instructions):

- **Q1 Section A:** *"the subclass, constructor, method overriding, validation, service-layer logic and collection operations."*
- **Q2 Section B:** *"the relevant controllers, services and exception-handling components provided in the starter project."*
- **Section C:** *"Run the application and verify the required API endpoints and expected behaviours using the permitted testing interface."*

## The night before: laptop checklist

Do every step **on the laptop you'll use in the exam**.

- [ ] **IntelliJ IDEA Ultimate is licensed**, not on a trial. Claim it with your IIT email at jetbrains.com/student, then sign in inside IntelliJ.
- [ ] **IntelliJ is up to date** (*Help → Check for Updates*). Java 25 needs **2025.2 or newer**.
- [ ] **JDK 25 is installed.** In IntelliJ: *File → Project Structure → SDK → Download JDK → Version 25*. Or install Temurin 25 and tick *Set JAVA_HOME*. In a terminal, `java -version` must print 25.
- [ ] **AI is switched off.** *Settings → Plugins → Installed* → disable **AI Assistant**, **JetBrains AI**, **Junie**, **GitHub Copilot** and **Full Line Code Completion**. Normal autocomplete stays.
- [ ] **Dry run.** Open the module's MODERATE mock starter (or `products-api` from the tutorials) and let Maven load. That downloads Spring Boot 4 into `~/.m2`, so exam day is fast. Check *Project Structure → SDK* = **25**. Run the app, and open Swagger.
- [ ] **Maven runs on 25.** If you see `release version 25 not supported`: *Settings → Build Tools → Maven → Runner → JRE* = **25**.
- [ ] **Practise the ZIP:** stop the app, right-click the project folder → *Compress to ZIP file*, open the ZIP, and check `pom.xml` and `src` are inside.
- [ ] **Blackboard:** log in on the laptop's browser. Check the password and MFA work.
- [ ] **Windows Update paused** (*Settings → Windows Update → Pause 1 week*). A forced restart mid-exam would be a disaster.
- [ ] **Do not disturb** on. **Charger** packed.

## Before you open the exam

- [ ] Close every IntelliJ project (*File → Close Project*). *"Only the starter project provided for this assessment may be opened."* *"You must not have more than one project open."*
- [ ] **Don't open `OOP-practice`.** Your own earlier code is banned.
- [ ] Quit WhatsApp, Discord, Claude and every other chat app.
- [ ] **One desktop.** Press Win+Tab and close any extra virtual desktops. *"The use of multiple desktops, virtual desktops, Spaces, workspaces … is strictly prohibited."*
- [ ] Put this guide and the refresher away. Personal notes are banned.

## Timeline: minute by minute

| Minute | Do | Checkpoint |
|---|---|---|
| **0–5** | Download and extract the starter. *File → Open* the folder that holds `pom.xml`. Let Maven load. Check SDK = 25. Run the app once and open Swagger: proof the starter works. | Starter runs |
| **5–10** | Read the **whole** paper once. Note the illustrative values: they are your Section C checks. | Plan in your head |
| **10–55** | **Q1 Section A.** The one new class (Q1(a)), constructor + `super(...)`, the overrides, validation, the service logic and collections. Keep it **compiling** after every method. | Section A done by **~55** |
| **55–85** | **Q2 Section B.** The controller endpoints, the exception handler, and any service methods left. | Section B done by **~85** |
| **85–110** | **Section C.** Restart the app. In Swagger, check every illustrative value, then one invalid case per endpoint. Fix the smallest thing and re-check. | Stop at **110** |
| **110–120** | **ZIP + upload only.** No more coding. | Uploaded |

Behind schedule? Get every method compiling with a simple, honest attempt, then move on. A project that doesn't start scores nothing in Section C.

## The rules

**Allowed:**

- *"Module lecture slides."*
- *"Learning materials available on Blackboard."*
- The starter project's **Swagger UI**.

**Banned:**

- *"Personal notes or unauthorized reference materials."*
- *"AI assistants and AI-powered coding tools, including GitHub Copilot and IntelliJ IDEA AI Assistant."*
- *"AI-based code generators or other automated code-generation tools."*
- *"Messaging applications or communication platforms."*
- *"Source code or solutions belonging to other individuals."*
- *"Your own previously developed source code or personal project implementations."*
- Talking to anyone: *"You must complete the assessment independently and in silence."*

**Code rules:**

- Create **exactly ONE** new Java class: the one named in **Question 1(a)**.
- Write everything else under the existing `// TODO` comments.
- *"Do not create additional Java classes or modify the supplied configuration, repository, request or response classes unless explicitly instructed in the examination paper."*
- **Don't add dependencies** to `pom.xml`. Swagger is already there. A wrong version stops the app from starting.

## Conventions from the module's mock

The module's MODERATE mock shows what the real starter probably looks like. Some of it is **different from the practice pack**:

| Topic | Module mock | Practice pack (d01–d11) |
|---|---|---|
| Stack | Java 25, Spring Boot 4.1.1 | Java 17, Spring Boot 3.3 |
| Packages | `controller`, `exception`, `model`, `repository`, `service` | `dto`, `error`, `repo` as well |
| Request/response types | *"ordinary Java classes, not Java records"*; *"Do not create a dto package"* | records in `dto/` |
| Error handling | `GlobalExceptionHandler` + `ErrorResponse` | `ApiExceptionHandler` + `ApiError` |
| URLs | no `/api` prefix, e.g. `/missions/{id}/energy` | `/api/...` |
| Port | `http://localhost:8080` | 8080 (P1–P3: 8081–8083) |
| Swagger | `http://localhost:8080/swagger-ui.html` | `/swagger-ui/index.html` |
| Arithmetic | *"Use long for intermediate arithmetic and convert safely to the required int"* | same idea (d03) |

What it means for your code:

- A plain response class needs **getters**. Jackson builds the JSON from `getX()` methods, so without them the body comes back as `{}`.
- A request class (read by `@RequestBody`) needs a **public no-arg constructor and setters**, or a constructor Jackson can use.
- Safe `int`: `Math.toIntExact(longValue)` throws if it doesn't fit, so there's no silent overflow.
- Use the names the starter gives you. If it has `GlobalExceptionHandler`, fill in that one. Don't create your own.

## Swagger UI in 5 steps

1. Run the app (green triangle on the `@SpringBootApplication` class). Wait for `Started ...Application`.
2. Open `http://localhost:8080/swagger-ui.html`. If that's a 404, try `/swagger-ui/index.html`. The port is in `application.properties`.
3. Click an endpoint → **Try it out**.
4. Fill in the boxes: one per `@PathVariable` and `@RequestParam`. For a POST, replace the example **Request body** with the paper's JSON. Then click **Execute**.
5. Read **Server response → Code** first (200, 201, 400, 404 …), then the **Response body**.

**Trap:** the **Responses** table under it only lists the codes Swagger *expects*. It is **not** what your app answered.

**Request URL** shows exactly what was called. An unexpected 404 usually means the mapping path is wrong.

## The Section C check loop

```
1. RUN      restart the app (it never sees edits until you restart)
2. HIT      the paper's request, in Swagger
3. COMPARE  status first, then each field, then the order of any list
4. READ     a 500? read the Run window: the first line, then the first line of YOUR package
5. FIX      the smallest thing
6. AGAIN    restart and re-check, plus one value that was already right
```

For every endpoint, check the **paper's illustrative values** first, then one **invalid** case: an unknown id (404), a value out of range (400), a duplicate (400 or 409, whatever the paper says). A **POST** must answer **201**, and the new item must show up in a later GET.

## ZIP and upload (minutes 110–120)

1. **Stop coding at 110.** Save everything (Ctrl+S), then stop the app (red square).
2. Find the project folder: the one with `pom.xml` and `src` directly inside. In IntelliJ, right-click the top folder → *Open In → Explorer*.
3. Right-click that folder → *Compress to ZIP file*. Zip the **whole folder**: *"Do not submit only .java files."*
4. Double-click the ZIP and check that `pom.xml`, `src/main/java/...` and `src/main/resources/application.properties` are inside, plus the **new class** you made.
5. **Upload the ZIP to Blackboard.** Wait for the confirmation.
6. A problem with the upload? **Raise your hand straight away.**

## If something breaks

| Problem | Do |
|---|---|
| IntelliJ, JDK, Maven or Blackboard won't work | **Raise your hand.** *"Do not close IntelliJ IDEA or log out of the computer."* Invigilators help with technical problems only, not code. |
| The app won't start | Read the Run window from the top. Find the **first** `Caused by:` and the first line in your own package. Usually: a TODO still throwing at startup, a duplicate mapping, or a compile error. |
| `Port 8080 was already in use` | An old run is still going. Stop it (red square in the Run window), then run again. |
| Swagger page 404 | Try the other path (`/swagger-ui.html` or `/swagger-ui/index.html`). Check the port. |
| Endpoint answers 500 | An exception escaped. Is it your own exception? Then the handler is missing `@ExceptionHandler(ThatException.class)`. |
| Endpoint answers 404 but should exist | The mapping path is wrong. Compare Swagger's **Request URL** with the paper's. |
| The JSON body is `{}` | The response class has no getters. |
| A code change makes no difference | You didn't restart the app. |
