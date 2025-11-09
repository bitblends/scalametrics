---
title: Return-Type Explicitness
description: Learn about the importance of explicit return types in Scala methods and how they contribute to API
keywords: [ return type explicitness, API stability, Scala, code maintainability, type inference ]
layout: doc
toc: true
---

# Return-Type Explicitness

For a method def `f(...) = expr`, the compiler infers the result type from expr. For a method `def f(...): T = expr`,
the result type is declared (explicit).

## What it Measures

Whether a  (`def f: T = ...`) explicitly declares a return type (`T = ...`) vs. relies on inference.

## How it's Measured

- **Declared** (explicit): Any `def` (including abstract defs which must declare a type) defining a return type.
- **Inferred**: Concrete defs without an explicit return type.
- **Scope**: Counts `public` members only (no private/protected or qualifiers like `private[foo]`).
- **Exclusions**: Excludes local defs (inside methods), constructors, and overrides (which must match the overridden
  signature).
- **`val`/`var`**: Tracked via their ascribed type (`val x: T = ...`).

## Why it Helps

- API stability: Public methods with inferred types can silently change when implementation changes (or when
  dependencies upgrade), leaking new types to users.
- Binary/source compatibility: Explicit types reduce accidental API changes and make MiMa-style checks more meaningful.
- Compile speed & errors: Explicit types often improve error messages and can speed compilation in generic/inline-heavy
  code.
- Docs & readability: Clear signatures are easier to read, especially for generic and higher-kinded code.

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

## Return Type Explicitness: Stable Contracts

For **public** members, explicit return types are a **contract** with users. They prevent accidental API drift during
refactors, improve IDE surfacing, and make binary/source compatibility safer. Inside private helpers and locals,
inference keeps code pleasant without hurting stability.

### 1. Public API (explicit)

The explicit type sets expectations and protects callers from a future change (e.g., switching to `Vector[Token]`).
Tooling can also find the API more easily.

=== "Before"

    ```scala
    def tokens(s: String) = Lexer.run(s)  // inferred
    ```

=== "After"

    ```scala
    def tokens(s: String): List[Token] = Lexer.run(s)
    ```

<!-- @formatter:off -->
!!! tip
    Explicit types become **documentation** and improve IDE discoverability.
<!-- @formatter:on -->

### 2. Private helper (inferred is ok)

Internal details can stay inferred to keep code compact. If a helper becomes widely used or public, promote its type
annotation then.

```scala
private def clamp(x: Int) = math.max(0, math.min(255, x))
```

<!-- @formatter:off -->
!!! note
    Keep public-facing members explicit; allow inference inside implementations.
<!-- @formatter:on -->


### 3. Common pitfalls

- Recursive definitions that rely on inference can accidentally change shape; annotate them.
- Extension methods in public APIs should have explicit result types for discoverability.
- Public givens/implicit defs benefit from explicit types to control resolution and error messages.
