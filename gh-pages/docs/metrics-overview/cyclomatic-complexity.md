--- 
title: Cyclomatic Complexity
description: Understanding and managing cyclomatic complexity in code.
keywords: [cyclomatic complexity, code quality, maintainability, testing, Scala]
layout: doc
toc: true
---

# Cyclomatic Complexity (McCabe)

Cyclomatic complexity is the number of independent paths through a function. It was developed by Thomas J. McCabe, Sr.
in 1976. Read more in [Wikipedia](https://en.wikipedia.org/wiki/Cyclomatic_complexity).

## What it Measures

The number of independent paths through a function: Higher = more branching = more paths to test and reason about.
Uses a graph-theoretic formula $M=E−N+2P$ (edges, nodes, components). In practice, approximate by **counting decision
points**.

## How it's Measured

- `if`, `else if`
- `match` cases (each non-default `case`)
- Loops: `while`, `do while`, `for` (including guards)
- `catch` clauses
- Short-circuit booleans: `&&`, `||`

## Why it Helps

- Identifies refactoring hotspots and code hardest to test/maintain.
- Enables quality gates (e.g., warn when $M > 10$).

---
<style>
/* Scope to content area to avoid leaking into the entire site */
.md-typeset {
  /* Badge tokens use theme variables for automatic light/dark harmony */
  --badge-fg: var(--md-default-fg-color);
  --badge-bg: var(--md-code-bg-color);
  --badge-border: var(--md-default-fg-color--light);
  --badge-warn-fg: var(--md-warning-fg-color);
  --badge-warn-bg: var(--md-warning-bg-color);
  --badge-warn-border: var(--md-warning-fg-color);
  --badge-note-fg: var(--md-accent-fg-color);
  --badge-note-bg: color-mix(in oklab, var(--md-accent-fg-color) 12%, var(--md-default-bg-color));
  --badge-note-border: var(--md-accent-fg-color);
  --panel-border: var(--md-default-fg-color--light);
  --panel-bg: var(--md-default-bg-color);
}

/* Minimal, theme-aware badges */
.md-typeset .badge {
  display: inline-block;
  padding: .15rem .45rem;
  border-radius: .5rem;
  font-size: .72rem;
  font-weight: 700;
  line-height: 1;
  color: var(--badge-fg);
  background: var(--badge-bg);
  border: 1px solid var(--badge-border);
  margin-left: .25rem;
  white-space: nowrap;
}
.md-typeset .badge.warn {
  color: var(--badge-warn-fg);
  background: var(--badge-warn-bg);
  border-color: var(--badge-warn-border);
}
.md-typeset .badge.note {
  color: var(--badge-note-fg);
  background: var(--badge-note-bg);
  border-color: var(--badge-note-border);
}

/* Optional panel styling for callouts that aren't admonitions */
.md-typeset .panel {
  border: 1px solid var(--panel-border);
  border-radius: .5rem;
  background: var(--panel-bg);
  padding: .8rem 1rem;
}
</style>

## Quick refactoring recipes

Refactoring for lower *perceived* complexity is mostly about **making decisions obvious**. The goal isn’t to chase a
smaller number at any cost—it’s to shape the code so the decisions are easy to see, name, and test. Here are some small
but effective moves to consider:

### 1. Small decision tree <span class="badge">Complexity = 3</span>

=== "Before"

    ```scala
    def sign(x: Int): Int =
      if (x > 0)  1
      else if (x < 0) -1
      else 0
    ```

    **Counting:** base 1 + `if` (1) + `else if` (1) ⇒ **3**.

    **Why it’s fine:** Three independent cases, three straightforward tests. Complexity mirrors the domain split.

=== "After"

    ```scala
    // No change needed: clarity matches the domain
    def sign(x: Int): Int =
      if (x > 0)  1
      else if (x < 0) -1
      else 0
    ```

<!-- @formatter:off -->
!!! tip "Testing heuristic"
    At minimum, cover `x > 0`, `x < 0`, and `x == 0`.
<!-- @formatter:on -->

### 2. Boolean soup vs. named predicates <span class="badge warn">Complexity = 4</span>

Dense boolean expressions often pack several decisions into a single line. Extracting named predicates doesn’t reduce
the count of decisions, but it reduces the readability cost. Names like `isTransient` and `safeToRetry` carry domain
meaning
and make it easier to see what scenarios are being permitted. This, in turn, improves test ergonomics: you can design
cases that intentionally flip only one predicate at a time. The trade-off is a couple of extra lines; the gain is lower
cognitive load and fewer mistakes when the rule inevitably evolves.

=== "Before"

    ```scala
    def shouldRetry(status: Int, isIdempotent: Boolean, networkFlaky: Boolean): Boolean =
      if ((status == 502 || status == 503) && (isIdempotent || networkFlaky)) true
      else false
    ```

    **Counting:** base 1 + `||` (1) + `&&` (1) + second `||` (1) ⇒ **4**.

=== "After"

    ```scala
    def shouldRetry(status: Int, isIdempotent: Boolean, networkFlaky: Boolean): Boolean = {
      val isTransient = status == 502 || status == 503
      val safeToRetry = isIdempotent || networkFlaky
      isTransient && safeToRetry
    }
    ```

<!-- @formatter:off -->
!!! note "Same complexity, lower cognitive load"
    Naming intermediate predicates makes intent obvious and tests easier to write.
<!-- @formatter:on -->

### 3. Pattern matching: total & explicit <span class="badge">Complexity = 5</span>

Pattern matching puts branches next to the domain types so behavior is easy to audit. Even if the complexity stays the
same, extracting small helpers prevents any single case from ballooning into its own mini-program. This structure is
resilient as tokens grow—adding a new token becomes a compiler-guided change, not a scavenger hunt. If your analyzer
flags rising complexity here, it’s usually a nudge to either split handlers or promote behavior to the types themselves.

=== "Before"

    ```scala
    sealed trait Token
    case class Number(n: Int) extends Token
    case object Plus extends Token
    case object Minus extends Token
    case class Ident(name: String) extends Token

    def describe(t: Token): String = t match {
      case Number(n)        => s"number:$n"
      case Plus             => "plus"
      case Minus            => "minus"
      case Ident(name) if name.nonEmpty => s"id:$name"
      case _                => "other"
    }
    ```

    **Counting:** base 1 + four non-default cases ⇒ **5**.
    Pattern guard doesn’t add beyond the case for this rule set.

=== "After (extracted helpers)"

    ```scala
    private def handleNumber(n: Int): String = s"number:$n"
    private def handleIdent(name: String): String =
      if (name.nonEmpty) s"id:$name" else "other"

    def describe(t: Token): String = t match {
      case Number(n)   => handleNumber(n)
      case Plus        => "plus"
      case Minus       => "minus"
      case Ident(name) => handleIdent(name)
      case _           => "other"
    }
    ```

### 4. Loops & for-guards <span class="badge">Complexity = 3</span>

Guards inside `for`-comprehensions are real decision points, and mutation can distract from the core intent. Shifting to
collection combinators doesn’t necessarily lower complexity, but it does isolate it: the predicate is where the decision
lives; sum is just aggregation. This makes future tweaks—like changing the predicate or accumulating additional
stats—cleaner and safer. As a side effect, removing mutation cuts down on incidental state bugs and simplifies
property-based
testing.

=== "Before"

    ```scala
    def sumEvens(xs: List[Int]): Int = {
      var acc = 0
      for {
        x <- xs           // loop (+1)
        if x % 2 == 0     // guard (+1)
      } acc += x
      acc
    }
    ```

    **Counting:** base 1 + loop (1) + guard (1) ⇒ **3**.

=== "After (combinators)"

    ```scala
    def sumEvens(xs: List[Int]): Int =
      xs.filter(_ % 2 == 0).sum
    ```

<!-- @formatter:off -->
!!! tip
    Combinators don’t magically lower complexity, but they **isolate** it: the predicate owns the decision; the rest is plumbing.
<!-- @formatter:on -->

---

### 5. Exceptions vs. explicit control flow <span class="badge">3</span> → <span class="badge warn">4</span>

Exceptions can keep complexity *numerically* low while hiding control flow. The exception-based example looks smaller
numerically, but it hides control flow and couples normal behavior to failure mechanisms. The explicit match is one
notch more complex by the metric, yet it is easier to refactor (e.g., to return richer errors), easier to test (no
exception stubbing), and friendlier to readers who want to see all outcomes laid out. If performance matters, both
versions are similar for valid input, but the explicit version avoids the cost of constructing stack traces in
failure-heavy paths.

=== "Before (exceptions)"

    ```scala
    def parseAndDivide(a: String, b: String): Either[String, Int] =
      try {
        Right(a.toInt / b.toInt)
      } catch {
        case _: NumberFormatException => Left("bad number format")
        case _: ArithmeticException   => Left("division by zero")
      }
    ```

    **Counting:** base 1 + two `catch` arms ⇒ **3**.

=== "After (explicit)"

    ```scala
    def parseAndDivide(a: String, b: String): Either[String, Int] =
      (a.toIntOption, b.toIntOption) match {
        case (Some(x), Some(y)) if y != 0 => Right(x / y)
        case (Some(_), Some(0))           => Left("division by zero")
        case _                            => Left("bad number")
      }
    ```

    **Counting:** base 1 + three non-default cases ⇒ **4**.

### 6. Else-if tower → lookup table <span class="badge warn">5</span> → <span class="badge">1</span>

Code with many symmetric branches is brittle: every new case expands the ladder and invites mistakes in operator
precedence or copy/paste. Turning the branches into data collapses the complexity to **1** and moves the burden to a
clearly reviewable structure. Extending behavior is a safe, localized edit, and you can even load the table from
configuration or a resource file if it’s expected to change at runtime. The trade-off is that rule order (when relevant)
must be encoded explicitly, but for pure lookups, maps are ideal.

=== "Before"

    ```scala
    def mimeFor(ext: String): String =
      if (ext == "png") "image/png"
      else if (ext == "jpg" || ext == "jpeg") "image/jpeg"
      else if (ext == "gif") "image/gif"
      else "application/octet-stream"
    ```

    **Counting:** base 1 + `if` (1) + `else if` (1) + `||` (1) + another `else if` (1) ⇒ **5**.

=== "After"

    ```scala
    private val mime: Map[String, String] = Map(
      "png"  -> "image/png",
      "jpg"  -> "image/jpeg",
      "jpeg" -> "image/jpeg",
      "gif"  -> "image/gif"
    )
    def mimeFor(ext: String): String =
      mime.getOrElse(ext, "application/octet-stream")
    ```

### 7. Distribute decisions with ADTs/polymorphism <span class="badge">3</span> → <span class="badge">per‑method 1</span>

Centralizing branching at call sites makes those sites complex and fragile (every new case forces edits far from the
type’s definition.) By moving behavior into the types, each implementation stays trivial (complexity **1**), and the
compiler warns you if you forget to implement the operation for a new subtype. The call sites also become
self-documenting (`s.area`), which reduces the surface area where mistakes can creep in. If performance or allocation is
sensitive, note that this approach does not add overhead in Scala; it mainly influences structure and readability.

=== "Before (match)"

    ```scala
    sealed trait Shape
    case class Circle(r: Double) extends Shape
    case class Rect(w: Double, h: Double) extends Shape

    def area(s: Shape): Double = s match {
      case Circle(r)  => Math.PI * r * r
      case Rect(w, h) => w * h
    }
    ```

    **Counting:** base 1 + two cases ⇒ **3**.

=== "After (polymorphism)"

    ```scala
    sealed trait Shape { def area: Double }
    case class Circle(r: Double) extends Shape {
      def area: Double = Math.PI * r * r     // complexity 1
    }
    case class Rect(w: Double, h: Double) extends Shape {
      def area: Double = w * h               // complexity 1
    }
    ```

### 8. Encapsulate necessary complexity <span class="badge note">Internal: 15–20 (encapsulated)</span>

Some domains are naturally branchy (parsers, small interpreters). The goal isn’t to obliterate complexity, but to 
**contain** it. Keep the gnarly logic private, blast it with focused tests, and present a small, intention-revealing
public API that callers can use without branching. Over time, this boundary lets you refactor the internals (or even
swap algorithms) while preserving simple, stable contracts for users.

=== "Internal (complex)"

    ```scala
    // Private: complex but constrained
    private def parseFlags(args: List[String]): Flags = {
      // many branches, guards, loops...
    }
    ```

=== "Public API (simple)"

    ```scala
    def configure(args: List[String]): Either[String, Config] =
      Right(buildConfig(parseFlags(args)))
    ```

