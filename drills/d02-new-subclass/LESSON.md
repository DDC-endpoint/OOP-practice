---
id: d02
module: OOP
kind: drill
title: Creating a subclass from scratch
difficulty: 2
timeMinutes: 15
tasks:
  - { name: "new class in the right package, extends the parent", marks: 2 }
  - { name: "field, constructor chaining, getter", marks: 3 }
  - { name: "override the abstract method (ceiling rounding)", marks: 3 }
  - { name: "add the seed object to the repository", marks: 2 }
---

# d02 · Creating a subclass from scratch

> **Exam link:** LBP **Task 2** always starts with *"implement a new X extends Y"*. In every paper the starter gives you one finished subclass and makes you **create the second one yourself**, then add it to the repository seed (P1: `WeekendPass`, P2: `AirMission`, P3: `DeadlineWorkload`).

## The idea

Creating a new subclass always takes the same five steps:

1. **Create the file in the right package.** Right-click the `model` package → **New → Java Class** → `Annual`. The file must be `model/Annual.java` and start with `package edu.practice.d02.model;`.
2. **`extends` the parent.** IntelliJ then complains that the abstract methods aren't written yet. Press **Alt+Enter → Implement methods** to stub them.
3. **Add a `private final` field** for each new piece of data.
4. **Write the constructor:** `super(...)` first, then validate, then store.
5. **`@Override` the abstract method(s)** with this subclass's own rule.

Then do the step people forget in the exam: **add the new object to the repository seed.** The seed is a commented `TODO`, because the code can't compile until your class exists.

**Copy the sibling.** The starter's finished subclass (here `PayAsYouGo`) is your template. Same shape, different rule.

### Ceiling division (rounding up) with whole numbers

`39000 / 12` in Java is integer division. It throws the remainder away. To round **up** with positive numbers:

```java
int up = (a + b - 1) / b;      // works, but a + b can overflow for huge a
int up = (a - 1) / b + 1;      // same result for a >= 1, and never overflows
```

`Math.ceilDiv` would be nicer, but it only exists from Java 18. **The LBP uses Java 17.**

## Worked example

This one uses a different domain: a shop with `Product`, given as `FoodProduct`. You'd create `GiftCard`.

```java
package edu.practice.shop.model;               // step 1: right package

public class GiftCard extends Product {        // step 2: extends
    private final int valueCents;              // step 3: private final field

    public GiftCard(long id, String name, int valueCents) {
        super(id, name);                       // step 4: super first...
        if (valueCents <= 0) throw new IllegalArgumentException("value must be positive");
        this.valueCents = valueCents;          // ...then validate and store
    }

    public int getValueCents() { return valueCents; }

    @Override
    public int priceCents(int quantity) {      // step 5: own rule
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        return Math.multiplyExact(quantity, valueCents);
    }
}
```
Then, in the repository constructor: `products.put(2L, new GiftCard(2, "£50 card", 5_000));`

## Your task

Create **`edu.practice.d02.model.Annual`**, an annual gym membership:

- It **extends `Membership`**.
- Constructor: `Annual(long id, String member, int yearlyCents)`. Chain `id` and `member` to the parent. Reject `yearlyCents <= 0`.
- It has a `private final int yearlyCents` field and a `getYearlyCents()` getter.
- **`monthlyFeeCents(int visits)`** = `yearlyCents / 12`, **rounded up** to a whole cent. The number of visits **doesn't** change the fee, but negative visits are rejected (`IllegalArgumentException`).
- **TODO d02-4:** in `MembershipRepository`, add `new Annual(2, "Ben", 39_000)` under id `2`.

Run `AnnualTest`. At first every test fails with "Create class Annual in package edu.practice.d02.model", which is expected.

## Hints

<details><summary>Hint 1: the tests say "needs a public constructor taking Long, String, Integer"</summary>

Your constructor must be `public` and take exactly `(long id, String member, int yearlyCents)`, in that order.
</details>

<details><summary>Hint 2: what does 12,345 / 12 round up to?</summary>

12,345 / 12 = 1,028.75, so the answer is 1,029. Check your formula with `a = 12345, b = 12`: `(12345 - 1) / 12 + 1 = 1028 + 1 = 1029`. ✔ And for `a = 39000`: `38999 / 12 + 1 = 3249 + 1 = 3250`. ✔
</details>

<details><summary>Hint 3: the repository test still fails</summary>

Did you add it with the key `2L`? `memberships.put(2L, new Annual(2, "Ben", 39_000));`
</details>

<details><summary>Hint 4: the fee method (nearly the answer)</summary>

```java
@Override
public int monthlyFeeCents(int visits) {
    if (visits < 0) throw new IllegalArgumentException("visits cannot be negative");
    return (yearlyCents - 1) / 12 + 1;
}
```
</details>

## Viva questions

<details><summary>What does <code>@Override</code> do, and is it required?</summary>

It tells the compiler "this replaces a parent method". The code compiles without it, but with it, a typo in the method name becomes a compile error instead of a silent new method. Always use it.
</details>

<details><summary>Why did the repository seed have to wait until your class existed?</summary>

`new Annual(...)` doesn't compile until the class `Annual` exists. That's why the starter leaves it as a comment.
</details>

<details><summary>Why compute in cents (int) instead of pounds (double)?</summary>

`double` can't store most decimals exactly (0.1 + 0.2 ≠ 0.3), so money drifts. Whole cents in an `int` are exact.
</details>

<details><summary>How is Annual different from PayAsYouGo, if both are Memberships?</summary>

Same parent, same method signature `monthlyFeeCents(int)`, different rule. Pay-as-you-go multiplies by visits, and annual ignores visits. That's the setup for polymorphism (d04).
</details>
