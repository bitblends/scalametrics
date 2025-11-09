---
title: Inline & Implicit Analysis
description: Learn about inline and implicit usage metrics to assess metaprogramming practices and API ergonom
keywords: [ inline usage, implicit usage, metaprogramming, API ergonomics, Scala ]
layout: doc
toc: true
---

# Inline and Implicit Analysis

This pair of metrics surfaces metaprogramming and API ergonomics signals that are easy to miss in code review.

## What it Measures

- `inline` (Scala 3):
    - `inline def/val/var`: definitions with the inline modifier.
    - `inline` parameters declared as `inline p: T`.
- `using` count (Scala 3).
- `givens` (Scala 3):
    - All given instances (given definitions).
    - Given conversions: `given Conversion[A, B]`.
- `@inline` methods (Scala 2).
- `implicit` conversions: heuristic subset of implicit defs that look like `implicit def f(a: A): B` (single non-
  `implicit` parameter list, non-`Unit` result). These are the ones that most often surprise readers and JIT (Scala 2).

## Why it Helps

- Visibility into compile-time metaprogramming/optimizations; prevents overuse that may bloat the code or slows down
  compile.
- Surfaces API ergonomics; too many context requirements can harm discoverability.

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

## Inline & Implicit Usage: Power with Restraint

`inline` can erase tiny abstractions in hot paths, and `given`/`implicit`s enable elegant APIs. Both increase
compile‑time cleverness. Use them when they clearly help, document intent, and keep the surface area small.

### 1. Inline for tiny hot-path helpers

This removes call overhead and enables constant folding in simple cases. Keep inline bodies tiny and pure; larger bodies
hurt compile times and readability.

=== "Example"

    ```scala
    inline def fastAbs(x: Int): Int = if x >= 0 then x else -x
    ```

<!-- @formatter:off -->
!!! note
    Measure before/after. Keep inline bodies **small** and side-effect free.
<!-- @formatter:on -->

### 2. Given instances with clear names and scopes

Prefer local/module scopes so resolution is predictable. Tests can supply alternative givens, and call sites remain
explicit with `using` when needed.

=== "Good"

    ```scala
    trait Clock { def now(): Instant }
    given systemClock: Clock with
      def now(): Instant = Instant.now()

    def stamp(msg: String)(using Clock): String =
      s"${summon[Clock].now()}: $msg"
    ```

=== "Overreach (too implicit)"

    ```scala
    // Given in a very broad package object leaks everywhere
    given noisy: Clock = ???
    ```

<!-- @formatter:off -->
!!! warning
    Prefer **local** givens or module-level scopes; avoid surprising global instances.
<!-- @formatter:on -->

### 3. Document the magic

Anytime you lean on `inline` or `givens`/`implicits`, write one or two sentences explaining the why. Future maintainers will
thank you, and reviewers can assess whether the cleverness is worth it.