---
title: Lines of code (LOC) Statistics
description: "Learn about Lines of Code (LOC) as a software metric to assess codebase size and complexity."
keywords: [ lines of code, LOC, code metrics, software complexity, maintainability, Scala ]
layout: doc
toc: true
---

# Lines of Code

Simply put, Lines of Code (LOC) is a software metric used to measure the size of a codebase by counting the number of
lines in the source code files. It is often used to estimate the complexity, maintainability, and development effort
required for a software project.

## What it Measures

Total number of lines in the source code files, methods, and members.

## How it's Measured

Count all lines in the source code files.

## Why it helps

- Provides a basic measure of codebase size.
- Long functions tend to do too much; correlate with defects and test difficulty.
- Helps in estimating development effort and project scope.
- Can be used to track code growth over time.
- Aids in identifying potential areas for refactoring or simplification.

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

## LOC Statistics: Size as a Signal

LOC doesn’t measure quality, but it does correlate with **scope**, **review time**, and **risk**. Huge files often
hide multiple responsibilities, intertwine concerns, and make parallel work harder. Use LOC to prompt a conversation:
“Can we split this so each part has a clear purpose?”

### 1. Split by responsibility

Smaller modules create **seams** where tests, ownership, and performance work can focus. Reviewers can specialize, and
changes stop colliding. This also helps incremental refactors, move one responsibility at a time.

=== "Before"

    ```scala
    // 800-line Service.scala: routing, validation, business logic, persistence
    object Service {
      // ...
    }
    ```

=== "After"

    ```scala
    object Routes     // HTTP/Wire concerns
    object Validate   // pure checks
    object Domain     // business rules
    object Store      // persistence
    ```

<!-- @formatter:off -->
!!! tip
    Smaller modules reduce **blast radius** and accelerate reviews.
<!-- @formatter:on -->

### 2. Exclude generated and vendored code

Generated code (protobufs, SDKs) distorts metrics and hides real signals. Configure your analyzer to exclude these
directories and commit that configuration. Now your metrics reflect code you actually maintain.

### 3. Use LOC with churn to find hotspots

A medium‑sized file that changes constantly can be riskier than a large but stable one. Pair LOC with **recent change
count** to prioritize attention. Many teams find 20% of files absorb 80% of maintenance pain—start there.
