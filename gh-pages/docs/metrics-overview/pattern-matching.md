---
title: Pattern-Matching Complexity
description: Learn about pattern-matching complexity metrics and how they help identify complex decision logic in Scala
keywords: [ pattern-matching complexity, decision logic, code complexity, maintainability, Scala ]
layout: doc
toc: true
---

# Pattern-Matching Complexity

Pattern matching is expressive, but it can also hide a lot of branching and nesting. This metric family makes that
branching visible and quantifiable.

## What it Measures

- **Matches** — number of `match` expressions.
- **Cases** — total number of case arms across all matches.
- **Average cases per match** — cases / matches.
- **Guards** — number of guarded cases (`case ... if cond =>`).
- **Wildcards** — number of wildcard/default cases (`case _ =>`).
- **Maximum Match Nesting** — deepest nesting level of match inside match.
- **Nested Matches** — how many match nodes appear inside another match.

## How it's Measured

- In function slice:
    - Count `match`.
    - For each, count `case` lines (including `case _`).
    - Count guards (`case ... if ...`).
- (Optional) Exhaustivity requires sealed-hierarchy knowledge beyond basic SemanticDB scans.

## Why it helps

Points to complex decision logic—candidates for splitting, table-driven logic, or polymorphism.

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

# Pattern Matching Usage: Lean on the Type System

Pattern matching shines when your domain is a finite set of cases. **Sealed ADTs** let the compiler prove you’ve handled
them all. Guards express small refinements. As cases grow complex, extract helpers or move behavior into the types
themselves.

### 1. Make illegal states unrepresentable

Booleans and strings invite illegal combinations (`kind="card", valid=false` but still processed). ADTs move invalid
states out of the realm of possibility. Pattern matches over `Payment` become self‑documenting and safe.

=== "Before (booleans + strings)"

    ```scala
    case class Payment(kind: String, valid: Boolean, amount: BigDecimal)
    ```

=== "After (ADT)"

    ```scala
    sealed trait Payment { def amount: BigDecimal }
    case class Card(amount: BigDecimal, last4: String) extends Payment
    case class Wire(amount: BigDecimal, iban: String)  extends Payment
    case class Cash(amount: BigDecimal)                extends Payment
    ```

**Usage:**

```scala
def fee(p: Payment): BigDecimal = p match {
  case Card(a, _) => a * 0.02
  case Wire(a, _) => a * 0.01
  case Cash(a) => 0
}
```


### 2. Pattern guards vs. nested blocks

Guards let you keep a small condition close to the case. As soon as a branch needs more than a couple of steps, a helper
keeps the match readable and testable.

=== "Guard"

    ```scala
    def label(id: String): String = id match {
      case s if s.nonEmpty => s"#$s"
      case _               => "unknown"
    }
    ```

=== "Helper"

    ```scala
    private def nonEmptyLabel(s: String): String =
      if (s.nonEmpty) s"#$s" else "unknown"
    ```

<!-- @formatter:off -->
!!! note
    Choose guards for **tiny predicates**; prefer helpers as soon as work grows.
<!-- @formatter:on -->

### 3. Custom extractors (unapply) for clarity

Extractors let you **speak the domain** in your matches. Instead of opaque regex checks, your cases say exactly what
you’re pulling out. Tests become simpler and intention revealing.

=== "Example"

    ```scala
    object Email {
      def unapply(s: String): Option[(String, String)] =
        s.split("@") match {
          case Array(user, host) => Some((user, host))
          case _                 => None
        }
    }

    def hostOf(s: String): Option[String] = s match {
      case Email(_, host) => Some(host)
      case _              => None
    }
    ```

### 4. When to prefer polymorphism

If you find a central `match` that keeps growing and is used all over the codebase, consider moving the behavior onto
the types (OO polymorphism) so call sites stop branching. Keep matches for one‑off decisions, constructors, and simple
routing.