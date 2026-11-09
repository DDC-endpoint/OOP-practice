---
id: p2
module: OOP
kind: past-paper
title: "P2 · Moderate · Emergency Dispatch Energy Planner API"
difficulty: 3
timeMinutes: 100
tasks:
  - { name: "Task 1 – Implement GroundMission", marks: 18 }
  - { name: "Task 2 – Add AirMission and implement dispatch planning", marks: 22 }
  - { name: "Task 3 – Validation, lookup and error handling", marks: 20 }
  - { name: "Task 4 – Expose three REST operations", marks: 20 }
  - { name: "Section B – Six functional checks", marks: 20 }
---

# 5COSC019W | Object Oriented Programming — Moderate Practice Exam

**100-minute sample practical | Emergency Dispatch Energy Planner API**

*Student practice paper • Entirely fictional example; NOT part of the live assessment*

## Exam instructions

- Individual timed practice: 100 minutes; maximum 100 marks. Challenge classification: **Intermediate**: polymorphic calculations, greedy selection, validation and deterministic ranking.
- Use the corresponding starter ZIP. Java 17 and the Maven Spring Boot project are required. Do not replace the parent class, DTO contracts or repository API unless a task explicitly instructs you to.
- This sample allows you to consult learning material while studying. The official examination may impose different restrictions: follow the approved Blackboard rules for the real sitting.
- Write and test your own solution before viewing the staff reference. Save frequently and submit a complete project ZIP in practice.
- Section A = 80 marks across four programming tasks (18/22/20/20); Section B = 20 marks across six functional checks. Attempt all tasks even if some parts do not compile.

## Scenario

An emergency coordination team allocates deliveries to ground vehicles or air vehicles. Each mission has its own energy formula and emergency priority. A limited energy budget must be assigned by a deterministic greedy algorithm.

Once you finish Task 2, seed `GroundMission(101,12,3,true)`, `GroundMission(102,4,2,false)` and `AirMission(103,8,1,3,true)`. All calculations use integer energy units.

## Files supplied

The starter includes a parent model class, one incomplete concrete model, in-memory repository, DTOs, a service class, a Spring REST controller and API error-handler scaffolding. Implement the stated TODOs; create the second concrete model class yourself. Use the repository seed comment to add sample objects after creating the new class.

## Assessment and marking

| Component | Marks | Assessed outcome |
|---|---:|---|
| Task 1 – First subclass | 18 | LO1 – OOP principles |
| Task 2 – New subclass + object processing | 22 | LO1 – OOP principles |
| Task 3 – Service implementation and errors | 20 | LO2 – API implementation |
| Task 4 – Controller/API integration | 20 | LO2 – API implementation |
| Section B – Functional testing | 20 | LO3 – Running implementation |
| **Total** | **100** | |

## Task 1 – Implement GroundMission [18 marks, LO1]

- **4 marks:** extend `DispatchMission`, use constructor chaining and expose `boolean medical` through `isMedical()`.
- **8 marks:** `energyUnits()` = 2×distanceKm + 5×loadKg, using long intermediate arithmetic and safe conversion to int.
- **6 marks:** `priority()` = 3 for a medical mission and 1 otherwise; both methods override the abstract parent methods.

## Task 2 – Add AirMission and implement dispatch planning [22 marks, LO1]

- **7 marks:** implement `AirMission extends DispatchMission` with `windPenalty` and `critical` properties; provide constructor and getters.
- **7 marks:** `energyUnits()` = 3×distanceKm + 8×loadKg + 2×windPenalty; `priority()` = 5 if critical, otherwise 2.
- **8 marks:** implement `DispatchService.plan(capacity, urgentOnly)`: optionally include only priority ≥ 3; sort priority DESC, energy ASC, ID ASC. Greedily select any mission that fits remaining budget and record unselected IDs in skip order. Return `DispatchPlan`.

Do not use concrete subclass type checks in the planning algorithm.

## Task 3 – Validation, lookup and error handling [20 marks, LO2]

- **6 marks:** implement `energy(id)`, raising `MissionNotFoundException` for unknown mission IDs.
- **7 marks:** `addAir` validates positive ID and distance, non-negative load and wind penalty, unique ID, and values within 0–10,000; calculate energy before saving to catch overflow.
- **7 marks:** reject non-positive plan capacity, return HTTP 400 for invalid input and HTTP 404 for missing mission; use `ApiError` with `BAD_REQUEST` / `NOT_FOUND`.

## Task 4 – Expose three REST operations [20 marks, LO2]

- **5 marks:** `GET /api/missions/{id}/energy` returns a JSON object containing `energyUnits`.
- **5 marks:** `POST /api/missions/air` accepts a `NewAirMission` JSON record and returns HTTP 201 with the created mission.
- **10 marks:** `GET /api/missions/plan?capacity=60&urgentOnly=false` returns `capacity`, `energyUsed`, `energyRemaining`, `selected` and `skipped` using the service algorithm. `urgentOnly` defaults to false.

## Illustrative calculations and input

- Energy: GroundMission(101) = 2×12 + 5×3 = 39.
- Energy: AirMission(103) = 3×8 + 8×1 + 2×3 = 38.
- Capacity=60, urgentOnly=false: sorted missions [103,101,102]; select 103 (38), skip 101 (39 does not fit), select 102 (18). Energy used=56, remaining=4.

## REST operations to demonstrate

```
GET  /api/missions
GET  /api/missions/101/energy
GET  /api/missions/plan?capacity=60&urgentOnly=false
POST /api/missions/air   JSON {"id":104,"distanceKm":6,"loadKg":1,"windPenalty":2,"critical":false}
```

## Section B – Six functional checks [20 marks, LO3]

Each functional check is scored when the running application demonstrates the required output/behaviour. The checks are independent of Section A source-code credit.

| Marks | Check |
|---:|---|
| 3 | GroundMission(101) consumes 39 energy units and has priority 3. |
| 3 | AirMission(103) consumes 38 units and has priority 5. |
| 3 | At capacity 60, greedy dispatch selects IDs [103,102] and skips [101]. |
| 3 | With urgentOnly=true, the routine mission is excluded from planning. |
| 4 | Bad capacity and invalid POST input return 400; an unknown ID returns 404. |
| 4 | Successful POST returns 201, and subsequent planning incorporates the new mission. |

## Submission and final checks

- Start the application and exercise both normal and invalid cases using Postman, curl or IntelliJ HTTP Client.
- Zip the complete Maven project folder (including `pom.xml`, sources, resources and any test files). Do not submit only `.java` files.
- Before submission, confirm that your ZIP contains the most recent source files and can be extracted successfully.

---

## How to attempt and mark

1. Open `past-papers/P2-drone` in IntelliJ (the folder with `pom.xml`), set a 100-minute timer, and work only in this project.
2. When time is up, mark your attempt from the repo root:

   ```
   java marking/Mark.java p2 past-papers/P2-drone
   ```

   or on Windows: `marking\mark.cmd p2 past-papers\P2-drone`

   The marker copies its tests into `src/test/java/marking/`, runs them through the Maven wrapper, and prints a mark per task out of 100. It checks the rules and illustrative values above plus extra inputs not shown in the paper, so hard-coded answers fail. It's an estimate: staff marking and the viva can differ.
