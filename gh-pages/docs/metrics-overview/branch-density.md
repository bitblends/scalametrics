---
title: Expression Branch Density Analysis
description: Learn about expression branch density analysis and how it helps improve code readability and maintainability.
keywords: [ branch density, code complexity, readability, maintainability, Scala ]
layout: doc
toc: true
---

# Expression Branch Density Analysis

It’s a normalization of branching constructs by code size. Instead of just saying “this function has 12 branches,” you
ask: how dense is the branching relative to how much code there is?

Based on this simple formula:

$$
density= \frac{100 \times\sum{tokens}}{\textit{LOC}}
$$

Where `tokens` counts the main branching/decision constructs:

- `if` (each `if` or `else if`)
- `match` cases (each `case` counts +1)
- loops: `while`, `do … while`, `for` / `for … yield`
- `catch` cases
- short-circuit boolean ops: `&&`, `||`

## What it Measures

Branch tokens per `100 LOC`:

$$ density = \frac{100 \times (if + case + while + for + catch + \&\& + ||)}{ \textit{LOC} } $$

## How it's Measured

Reuses counts from cyclomatic token scan, divided by LOC.

## Why it Helps

- **Separates long vs. tangled**: two 80-line functions may be very different; density highlights “branchy” logic.
- **Refactoring targets**: high density often means multiple intertwined decisions → candidates to split, table-drive,
  or push into ADTs/polymorphism.
- **Performance hints**: in hot paths, dense branching can inhibit JVM inlining/prediction; helps keep kernels
  straight-line where possible.

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

## Expression Branch Density: Make Decisions Legible

Expression branch density highlights **how many decisions** are packed into a small stretch of code, not just how many
there are. Density doesn’t always change complexity, but it **changes readability** and testability. Spread decisions
out, give them names, and prefer structures that readers already recognize.

### 1. Name the logic

The original line requires mentally evaluating operator precedence and short-circuiting. By naming each sub-decision, we
turn an eye-chart into three testable statements. When the policy changes (e.g., add `Auditor`), there’s an obvious
place to edit and an obvious test to update.

=== "Before"

    ```scala
    def allow(u: User, req: Req): Boolean =
      (u.role == Admin || u.role == Editor) && !req.readonly && (req.ip.isTrusted || u.isOnCall)
    ```

=== "After"

    ```scala
    def allow(u: User, req: Req): Boolean = {
      val roleOk   = u.role == Admin || u.role == Editor
      val modeOk   = !req.readonly
      val originOk = req.ip.isTrusted || u.isOnCall
      roleOk && modeOk && originOk
    }
    ```

<!-- @formatter:off -->
!!! tip
    Same decisions, but now they’re **addressable** in tests (`roleOk`, `modeOk`, `originOk`).
<!-- @formatter:on -->

### 2. Replace nested ternaries/ifs with a lookup table

=== "Before"

    ```scala
    def level(code: Int): String =
      if (code < 0) "err" else if (code == 0) "warn" else "info"
    ```

=== "After (ordered rules)"

    ```scala
    case class Rule(p: Int => Boolean, out: String)
    val rules = List(
      Rule(_ < 0, "err"),
      Rule(_ == 0, "warn"),
      Rule(_ => true, "info")
    )
    def level(code: Int): String = rules.find(_.p(code)).get.out
    ```
