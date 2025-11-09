---
title: Documentation Coverage
description: Learn about documentation coverage metrics and their importance in code maintainability and readability.
keywords: [ documentation coverage, code quality, maintainability, readability, Scala ]
layout: doc
toc: true
---

# Documentation Coverage

Measures the extent to which code elements (classes, methods, functions, etc.) are documented with comments or
documentation strings.

## What it Measures

Percentage of code elements that have associated documentation comments.

## How it's Measured

- Identify code elements such as classes, methods, functions, and modules.
- Check for the presence of documentation comments (e.g., docstrings, Javadoc-style comments).
- Calculate the ratio of documented elements to the total number of elements.

## Why it Helps

- Encourages better documentation practices.
- Improves code maintainability and readability.

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

## Communicate Intent

Coverage measures **where** docs exist; usefulness depends on **what they say**. Good docstrings answer: what does this
do, how do I use it, and what are the surprises or edge cases? Prioritize public APIs and anything that users might need
to import implicitly. Document **public** types/methods, especially at library boundaries and implicit/given instances.

### 1. Document a public API

A few lines prevent hours of guesswork. The example anchors expectations; the thrown exception clarifies failure modes.
Keep comments **close** to code so they travel with refactors.

=== "Before"

    ```scala
    def parse(s: String): Result
    ```

=== "After"

    ```scala
    /** Parses a DSL document from UTF-8 text.
      * @param s full document text
      * @return a parsed result; errors include 1-based line/column
      */
    def parse(s: String): Result
    ```

### 2. Explain givens/instances

Context parameters and conversions are invisible at call sites—documentation is your primary defense against surprise.

```scala
/** Low-priority fallback instances for JSON encoding. */
given fallbackEncoder: Encoder[Foo] = ???
```

<!-- @formatter:off -->
!!! tip
    Keep docstrings short, but answer: **what**, **when to use**, and **surprises**.
<!-- @formatter:on -->

### 3. Keep docs alive

Make it easy to update docs during code changes: short, local comments; doc tests; pre‑commit hooks that lint stale
references. Coverage is the dashboard, but the habit is what keeps readers happy.
