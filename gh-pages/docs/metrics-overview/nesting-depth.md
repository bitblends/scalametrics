---
title: Nesting Depth Analysis
description: Learn about nesting depth as a software metric to assess code complexity and maintainability.
keywords: [ nesting depth, code complexity, maintainability, readability, Scala ]
layout: doc
toc: true
---

# Nesting Depth

Nesting depth is the maximum number of nested control/scope constructs you must mentally “hold” while reading a
function. Deeper nesting increases cognitive load and the risk of bugs. Flattening (early returns, guard clauses, small
helpers) usually improves readability and testability. Read more
in [WikiPedia](https://en.wikipedia.org/wiki/Nesting_(computing)#In_programming).

## What it Measures

Deepest level of nested blocks/branches. Highly nested code is harder to read and maintain.

## How it's Measured

- Counts depth for:
    - **Blocks**: `{}`
    - **Conditionals**: `if (...) else (...)`
    - **Pattern matching**: each `case` body
    - **Loops**: `while`, `do ... while`, `for`, `for ... yield`
    - **Exception handling**: `try` body, each `catch` case body, and `finally` body
    - **Anonymous functions**: the lambda body

## Why it Helps

Highlights places to flatten control flow (early returns, small helpers).

---
<style>
.md-typeset {
  --badge-fg: var(--md-default-fg-color);
  --badge-bg: var(--md-code-bg-color);
  --badge-border: var(--md-default-fg-color--light);
  --badge-warn-fg: var(--md-warning-fg-color);
  --badge-warn-bg: var(--md-warning-bg-color);
  --badge-warn-border: var(--md-warning-fg-color);
  --badge-note-fg: var(--md-accent-fg-color);
  --badge-note-bg: color-mix(in oklab, var(--md-accent-fg-color) 12%, var(--md-default-bg-color));
  --badge-note-border: var(--md-accent-fg-color);
}
.md-typeset .badge {
  display:inline-block; padding:.15rem .45rem; border-radius:.5rem; font-size:.72rem; font-weight:700;
  line-height:1; color:var(--badge-fg); background:var(--badge-bg); border:1px solid var(--badge-border); margin-left:.25rem; white-space:nowrap;
}
.md-typeset .badge.warn { color:var(--badge-warn-fg); background:var(--badge-warn-bg); border-color:var(--badge-warn-border); }
.md-typeset .badge.note { color:var(--badge-note-fg); background:var(--badge-note-bg); border-color:var(--badge-note-border); }
</style>

## Readability and Guard Clauses

Deep indentation increases cognitive load because the reader must hold multiple active contexts in mind—every extra
level is another state to track. The aim isn’t “no nesting ever,” it’s to keep **local depth** small so each unit of
code is quick to scan. Shallow functions are easier to review, easier to test, and easier to evolve.

### 1. From pyramid to guard clauses <span class="badge">Depth: 4 → 1–2</span>

Each `if` opens a new level, pushing the happy path deeper and deeper. Readers now have to match three braces and
remember which failure corresponds to which check. Bugs often creep in when you add a fourth condition and forget to
update the right branch.
Guard clauses **front‑load failure** and keep the success path flush-left. The function’s visual shape now communicates
intent: handle the edge cases, then proceed. Complexity hasn’t changed much, but **perceived** complexity drops
dramatically.

=== "Before"

    ```scala
    def process(u: User): Either[String, Receipt] = {
      if (u != null) {
        if (u.active) {
          if (u.cart.nonEmpty) {
            Right(checkout(u.cart))
          } else Left("empty cart")
        } else Left("inactive")
      } else Left("no user")
    }
    ```

=== "After (guards)"

    ```scala
    def process(u: User): Either[String, Receipt] = {
      if (u == null) Left("no user")
      if (!u.active) Left("inactive")
      if (u.cart.isEmpty) Left("empty cart")
      Right(checkout(u.cart))
    }
    ```

<!-- @formatter:off -->
!!! tip
    Guard clauses keep the **happy path flush-left** and make failure cases obvious.
<!-- @formatter:on-->

### 2. Pattern matching over nested `if`s <span class="badge">Depth: 4 → 2</span>

Branches are coupled in a way that hides which conditions truly belong together. It’s difficult to see that `admin` is
only relevant for status `200`.
The primary axis of decision (status code) is explicit; the secondary detail (admin) is scoped to its relevant case.
Depth falls, and tests map naturally to cases.

=== "Before"

    ```scala
    def classify(code: Int, admin: Boolean): String = {
      if (code == 200) {
        if (admin) "ok-admin" else "ok"
      } else {
        if (code == 401) "unauthorized" else "other"
      }
    }
    ```

=== "After (match + small helpers)"

    ```scala
    def classify(code: Int, admin: Boolean): String = code match {
      case 200 => if (admin) "ok-admin" else "ok"
      case 401 => "unauthorized"
      case _   => "other"
    }
    ```

### 3. Extract helpers to cap local depth <span class="badge">Depth per function: 1–2</span>

The happy path is clear, there’s no manual unwrapping, and indentation is capped. Combinators encode a common shape, so
future readers recognize the pattern quickly.

=== "Before"

    ```scala
    def render(p: Product): String = {
      if (p.stock > 0) {
        if (p.price > 100) s"🔥 ${p.name}" else s"${p.name}"
      } else {
        "sold-out"
      }
    }
    ```

=== "After (named helpers)"

    ```scala
    private def hotTag(p: Product): String =
      if (p.price > 100) s"🔥 ${p.name}" else s"${p.name}"

    def render(p: Product): String =
      if (p.stock <= 0) "sold-out" else hotTag(p)
    ```

<!-- @formatter:off -->
!!! note
    Depth is measured at the function level; extraction keeps each function shallow and testable.
<!-- @formatter:on -->