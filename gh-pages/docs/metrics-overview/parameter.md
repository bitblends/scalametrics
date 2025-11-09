---
title: Parameters & Arity Analysis
description: Learn about parameters and arity analysis to assess function interfaces and improve API design.
keywords: [ parameters, arity, function interface, API design, Scala ]
layout: doc
toc: true
---

# Parameters & Arity Analysis

These describe the shape of a function’s parameter interface.

## What it Measures

- **Total parameters** per `def` - sum of all params across all parameter lists.
- **Parameter lists** (curried arity) — how many (...) groups.
- **Defaulted params** — params with a default value (= ... in the declaration).
- **By-name params** — params typed as => `T` (lazy/only evaluated when used).
- **Context params** (`implicit`/`using`) - params that are implicit (Scala 2) or using (Scala 3).
- **Varargs** (repeated) params — `A*`.
- **Per-list** classification — which lists are context lists.

## How it's Measured

- From the function signature:
    - Counts `(...)` groups → parameter lists.
    - Within each group, counts parameters per list.
    - Detects `implicit` (Scala 2) / `using` (Scala 3).

## Why it Helps

- Large arity signals unclear responsibilities or missing domain objects.
- API ergonomics: High total parameter counts or many lists are friction at call sites.
- Readability & discoverability: Widespread implicit/using can be powerful, but too many context params harms
  discoverability; the metric makes it visible.
- Binary/behavioral stability: Defaulted params change call-site behavior (and can affect overload resolution). Tracking
  them helps avoid surprising changes.
- Performance semantics: By-name and varargs have runtime overhead/tradeoffs—good to use sparingly, especially in
  performance-sensitive libraries.
- Refactoring targets: Surfacing the “top N highest-arity” defs is a great way to find APIs that should be split or
  redesigned.

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

## Parameter Analysis: API Ergonomics

Parameters determine how pleasant a function is to call. Context parameters (`using`/`implicit`s) can remove
boilerplate, but they also couple call sites to ambient scope. Defaults help discoverability, but they can hide
important choices. Strive for a small, explicit surface that reads like prose.

### 1. From implicit surprise to explicit context

`using` makes the dependency visible at the call site while remaining concise. Prefer local `given` instances to avoid
global, surprising resolution. Tests can supply alternate contexts explicitly.

=== "Implicit (Scala 2)"

    ```scala
    def render(a: A)(implicit fmt: Format): String = ???
    ```

=== "Scala 3 `using` + `given`"

    ```scala
    def render(a: A)(using fmt: Format): String = ???
    given Format = defaultFormat
    ```

<!-- @formatter:off -->
!!! note
    Name contexts (`Format`, `Clock`, `Logger`) clearly; keep them **few** and **well-scoped**.
<!-- @formatter:on -->

### 2. Defaults and varargs

Replacing stringly‑typed parameters with small enums removes spelling mistakes and enables exhaustiveness in callers’
matches. Defaults still help ergonomics, but the choice is explicit and discoverable.

=== "Before"

    ```scala
    def log(msg: String, level: String = "info", tags: String*): Unit = ???
    ```

=== "After (explicit level type + small ADT)"

    ```scala
    enum Level { case Info, Warn, Error }
    def log(msg: String, level: Level = Level.Info, tags: String*): Unit = ???
    ```

### 3. Named arguments and builder alternatives

If a function has more than ~3 or 4 parameters, consider named arguments at call sites or move optional/rare options into a
small config object with sensible defaults. This reduces positional mistakes and keeps signatures approachable.
