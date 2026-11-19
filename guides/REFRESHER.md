---
id: refresher
title: LBP concept refresher
---

# LBP concept refresher

> **Read this on the way, and put it away before you go in.** Personal notes are banned in the exam room. Each part has the idea, a short example, and a one-line **viva** answer to say out loud.

## 1 · Inheritance and abstract classes

```java
public abstract class Parcel {                       // abstract: can't do new Parcel(...)
    private final long id;                           // private + getter = encapsulation
    protected Parcel(long id) { this.id = id; }
    public long getId() { return id; }
    public abstract int pricePence();                // no body: every subclass MUST override it
}

public class ExpressParcel extends Parcel {
    private final boolean fragile;                   // the subclass's own field
    public ExpressParcel(long id, boolean fragile) {
        super(id);                                   // FIRST line: the parent sets its own fields
        this.fragile = fragile;                      // then set yours
    }
    @Override public int pricePence() { return fragile ? 900 : 600; }
}
```

- `super(...)` must be the **first** statement in the constructor. The parent's `private` fields can only be set by the parent's constructor.
- `@Override` makes the compiler check that you really override a parent method. A typo becomes a compile error instead of a silent new method.

**Viva:** *"The subclass extends the abstract parent, passes the shared fields up with `super(...)`, and overrides the abstract methods with its own rules."*

## 2 · Polymorphism: no type checks

```java
for (Parcel p : repo.all()) {
    total += p.pricePence();          // Java runs the RIGHT override for each object
}
// NEVER: if (p instanceof ExpressParcel) ...   or   p.getClass() == ...
```

The variable's type is `Parcel`, but the **object's** type decides which `pricePence()` runs (dynamic dispatch). The papers ban `instanceof` and `getClass()` in the service. Use a method like `kind()` or `priority()` instead (d04).

**Viva:** *"I call the abstract method on the parent type, and each subclass answers for itself. Adding a new subclass needs no change to the service."*

## 3 · Encapsulation: plain classes, not records

The module's mock says *"ordinary Java classes, not Java records"*. Spring's JSON library (Jackson) **reads getters** to build the response, and uses a **no-arg constructor + setters** to read a request body.

```java
// RESPONSE class: private fields, constructor, getters
public class EnergyResponse {
    private final int energyUnits;
    public EnergyResponse(int energyUnits) { this.energyUnits = energyUnits; }
    public int getEnergyUnits() { return energyUnits; }        // → {"energyUnits":39}
}

// REQUEST class (@RequestBody): no-arg constructor + getters + setters
public class NewAirMission {
    private long id;
    private int distanceKm;
    public NewAirMission() { }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getDistanceKm() { return distanceKm; }
    public void setDistanceKm(int distanceKm) { this.distanceKm = distanceKm; }
}
```

Getter name → JSON name: `getEnergyUnits()` → `"energyUnits"`. A class with **no getters** → `{}`.

**Viva:** *"Fields are private so other classes go through methods. Jackson turns my getters into JSON fields."*

## 4 · Validation: reject bad input early

```java
if (weightGrams <= 0) throw new IllegalArgumentException("weightGrams must be positive");
if (code == null || code.isBlank()) throw new IllegalArgumentException("trackingCode is required");
if (repo.exists(id)) throw new IllegalArgumentException("parcel " + id + " already exists");
```

- Validate **before** you save. An invalid object must never reach the repository.
- `isBlank()` also catches `"   "`. `isEmpty()` doesn't.
- `IllegalArgumentException` → the handler turns it into **400 BAD_REQUEST**.

**Viva:** *"The service validates every field and throws IllegalArgumentException, so nothing invalid is stored, and the handler maps it to 400."*

## 5 · Integer maths

| Need | Write | Why |
|---|---|---|
| "every **started** 500 g" (ceiling) | `(grams - 1) / 500 + 1` | `grams / 500 + 1` is wrong at exact multiples (500 → 2) |
| percentage | `price * 10 / 100` | `price * (10 / 100)` is `price * 0` in int maths |
| no overflow | `long e = 3L * distance + 8L * load;` then `Math.toIntExact(e)` | `toIntExact` throws instead of silently wrapping |
| safe multiply | `Math.multiplyExact(a, b)` | throws `ArithmeticException` on overflow |

Always test the **boundary** (exactly 500, 501) and one value either side (d03, d10).

**Viva:** *"Integer division truncates, so I multiply before dividing, use the ceiling formula for 'every started block', and do the maths in long, then convert with toIntExact."*

## 6 · Collections and the repository

```java
@Repository
public class MissionRepository {
    private final Map<Long, Mission> missions = new ConcurrentHashMap<>();
    public Collection<Mission> all()   { return List.copyOf(missions.values()); }  // a safe copy
    public Mission find(long id)       { return missions.get(id); }                // null if absent
    public boolean exists(long id)     { return missions.containsKey(id); }
    public void save(Mission m)        { missions.put(m.getId(), m); }
}
```

- `ConcurrentHashMap`: the repository is **one shared bean**, used by many request threads at once.
- A primitive `long id` can **never be null**. Check the **object** `find(id)` returns: `if (m == null) throw new MissionNotFoundException(id);`
- An atomic "add if new": `missions.putIfAbsent(id, m)` returns the old value, or `null` if it added.

**Viva:** *"A Map keyed by id gives fast lookup. ConcurrentHashMap because the singleton repository is shared by request threads."*

## 7 · Streams

```java
List<Quote> quotes = repo.all().stream()
        .filter(p -> !urgentOnly || p.priority() >= 3)   // keep some
        .map(p -> toQuote(p))                            // change each one (Parcel → Quote)
        .sorted(order)                                   // order them
        .toList();                                       // collect

int total = quotes.stream().mapToInt(Quote::getPricePence).sum();
```

`filter` keeps or drops items, `map` turns one item into one other item, `sorted` orders them, and `toList()` ends the stream (d05, d06).

**Viva:** *"filter selects, map transforms each element, sorted orders with a Comparator, and toList collects the result."*

## 8 · Comparator chains

```java
Comparator<Mission> order = Comparator
        .comparingInt(Mission::priority).reversed()     // 1. priority HIGH → low
        .thenComparingInt(Mission::energyUnits)         // 2. energy low → high
        .thenComparingLong(Mission::getId);             // 3. id low → high
```

- `.reversed()` flips **everything built so far**. Put it straight after the key you want descending.
- **Trap:** `.reversed()` at the **end** also flips the tie-breaks (d05, d10): `[5, 2, 4, 3]` instead of `[5, 2, 3, 4]`.
- Use `comparingInt`/`thenComparingInt` for `int`, and `...Long` for `long` ids.

**Viva:** *"I chain the keys in priority order, and reverse only the first key so the tie-breaks stay ascending."*

## 9 · The greedy fill (selected + skipped)

```java
int remaining = capacity;
List<PlanLine> selected = new ArrayList<>();
List<Long> skipped = new ArrayList<>();
for (Mission m : sortedEligible) {          // the WHOLE sorted list, already filtered
    if (m.energyUnits() <= remaining) {
        selected.add(toLine(m));
        remaining -= m.energyUnits();
    } else {
        skipped.add(m.getId());             // didn't fit: note it, KEEP GOING
    }
}
```

- Don't `break` on the first one that doesn't fit. A later, smaller one may still fit.
- Items **filtered out** (e.g. by `urgentOnly`) go in **neither** list. `skipped` holds only eligible items that didn't fit.
- Mock example: capacity 60 → sorted `[103, 101, 102]` → select 103 (38), skip 101 (39), select 102 (18) → used 56, remaining 4.

**Viva:** *"I sort, then walk the full list taking each item that still fits, and record the ones that don't fit as skipped."*

## 10 · Layers and dependency injection

```java
@RestController @RequestMapping("/missions")
public class DispatchController {
    private final DispatchService service;
    public DispatchController(DispatchService service) { this.service = service; }  // injected
}
```

- **Controller:** HTTP in, HTTP out. One line per method that calls the service. No `if`, no loops, no maths.
- **Service:** all the rules: validation, calculations, sorting, "not found".
- **Repository:** stores the data.
- Spring creates **one** of each (`@Service`, `@Repository`, `@RestController`) and passes them into constructors. That's IoC + constructor injection.

**Viva:** *"The controller delegates to the service, so every rule lives in one place. Spring injects the service through the constructor."*

## 11 · Spring REST annotations

| Write | Means | Example |
|---|---|---|
| `@RestController` | this class answers HTTP with JSON | on the class |
| `@RequestMapping("/missions")` | the URL prefix for the class | `/missions/...` |
| `@GetMapping("/{id}/energy")` | GET on that path | `GET /missions/101/energy` |
| `@PostMapping("/air")` | POST on that path | `POST /missions/air` |
| `@PathVariable long id` | the `{id}` part of the path | `101` |
| `@RequestParam int capacity` | `?capacity=60`, **required** (missing → 400) | |
| `@RequestParam(defaultValue = "false") boolean urgentOnly` | optional, with a default | |
| `@RequestBody NewAirMission body` | the JSON body → an object | POST |

```java
@PostMapping("/air")
public ResponseEntity<AirMission> addAir(@RequestBody NewAirMission body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.addAir(body));   // 201
}
// or: @PostMapping("/air") @ResponseStatus(HttpStatus.CREATED) public AirMission addAir(...)
```

**Viva:** *"@PathVariable names which resource, @RequestParam is an option in the query string, and @RequestBody reads the JSON body. A create answers 201."*

## 12 · Exceptions → status codes

```java
public class MissionNotFoundException extends RuntimeException {
    public MissionNotFoundException(long id) { super("Mission not found: " + id); }
}

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
    }
    @ExceptionHandler(MissionNotFoundException.class)          // ← forget this line = 500 (d10)
    public ResponseEntity<ErrorResponse> notFound(MissionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }
}
```

The service **throws**, and the handler **translates** it. A method without `@ExceptionHandler` is never called, so the exception escapes as **500** (d08, d10).

**Viva:** *"The service throws meaningful exceptions; one @RestControllerAdvice maps each type to a status and an ErrorResponse body."*

## 13 · Status codes

| Code | Name | When |
|---|---|---|
| 200 | OK | GET; a POST that doesn't create |
| 201 | Created | a POST that adds something |
| 400 | Bad Request | invalid input (`IllegalArgumentException`), a missing required `@RequestParam` |
| 404 | Not Found | unknown id, or **no endpoint matches the URL** |
| 409 | Conflict | valid, but clashes with stored state (only if the paper asks) |
| 415 | Unsupported Media Type | the body wasn't sent as JSON |
| 500 | Internal Server Error | an exception escaped: a bug or a missing handler |

## 14 · DTO vs entity, and stateful operations

- **Model/entity** (`AirMission`): the thing stored, with its behaviour (`energyUnits()`).
- **Request/response class** (`NewAirMission`, `DispatchPlan`): the shape of the JSON going in or out. Data only, no rules. It hides fields the caller shouldn't see (d06).
- **Half-open intervals** `[start, end)`: two bookings overlap when `aStart < bEnd && bStart < aEnd`. Touching (`end == start`) is **not** an overlap (d11).
- **Preview never saves.** **Commit** saves once. The same key + the same request returns the **stored** result, and the same key + a different request → 409 (d11).

**Viva:** *"The model holds the data and the rules; the request and response classes only carry JSON between the client and the service."*

## 15 · Section C: check it like the examiner

```
RUN → HIT (Swagger: Try it out → Execute) → COMPARE → READ → FIX → AGAIN
```

- Read **Server response → Code**, not the "Responses" table.
- Check the paper's illustrative values, then one invalid case per endpoint.
- A POST → **201**, then a GET shows the new item.
- **Restart** the app after every change.

## 16 · Top 10 traps

1. A TODO still says `throw new UnsupportedOperationException(...)`. Delete it once the method is done.
2. `isEmpty()` where you need `isBlank()`: `"  "` slips through.
3. Comparing a `long` id to `null`. Check the object returned by `find(id)`.
4. `.reversed()` at the end of a chain flips the tie-breaks too.
5. Integer division: `x * (10 / 100)` is 0. Multiply first.
6. The handler method has no `@ExceptionHandler(...)`, so you get 500 instead of 404/400.
7. A POST returns 200 where the paper wants **201**.
8. A plain class with no getters gives a JSON body of `{}`.
9. A required `@RequestParam` with no `defaultValue`, so leaving it out gives 400.
10. Testing an old run. **Restart** the app after every edit.

Good luck. One class, TODOs only, keep it compiling, check in Swagger, ZIP at 110.
