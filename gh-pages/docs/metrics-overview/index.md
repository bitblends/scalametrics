---
title: Overview
description: "An introduction to the key metrics ScalaMetrics provides and how they help improve code quality."
keywords: [ code metrics, software quality, Scala, cyclomatic complexity, nesting depth, documentation coverage ]
layout: doc
toc: true
---

# Measuring What Matters: A Gentle Introduction

When a codebase is small, you "just know" where the tricky parts live. As it grows, intuition starts to blur. Reviews
take longer, bug fixes ricochet, and the same modules keep pulling attention. Metrics don’t replace judgment, but they
_sharpen_ it. They turn vague hunches into a crisp picture—so teams can spend less energy guessing and more time
improving the places that matter.

**ScalaMetrics** analyzes Scala 2.12, Scala 2.13, and Scala 3 source code and produces a set of focused measurements.
This page explains _why_
these measurements are useful, how to read them without getting lost in the numbers, and where to start if you want
quick wins without a grand rewrite.

---

## What metrics really do

Think of metrics as instruments on a dashboard. They won’t fly the plane, but they will tell you when you’re drifting,
climbing too steeply, or burning fuel faster than you thought. In practice, this means three things:

1. **They make risks visible earlier.** Instead of discovering a fragile module during a Friday night incident, you can
   see its complexity and churn rising weeks in advance.
2. **They give you a shared language.** “This function _feels_ messy” becomes “Nesting depth is seven and the branch
   density doubled after the last change—let’s extract a helper.”
3. **They help you track improvement.** Small, steady changes add up. When you can see the 90th percentile complexity
   easing downward and documentation coverage climbing, momentum becomes tangible.

Used poorly, metrics become vanity numbers. Used well, they help you direct attention, reduce surprises, and build a
codebase that new contributors can approach with confidence.

## The signals ScalaMetrics captures

ScalaMetrics focuses on a handful of measures that together paint a practical picture of complexity, readability, and
API
hygiene.

**Cyclomatic complexity** estimates the number of distinct paths through a function. High numbers aren’t “bad” on their
own—some problems are inherently branchy—but they do imply more cases to test and more ways for a future edit to miss an
edge case. When a function crosses from “busy” to “opaque,” complexity will usually be the first needle to twitch.

**Nesting depth** speaks to cognitive load. Deeply indented code asks the reader to keep more state in their head: which
`if` are we inside now? Which `match` branch? Flattening with early returns, using helper methods, or pushing conditions
into meaningful names often restores clarity without changing behavior at all.

**Expression branch density** notices the compact cleverness that hides in a single line. It’s the difference between a
chain of thoughtful steps and a one-liner that demands a mental debugger. If density rises, consider slowing the code
down into named values and intermediate checks—future you will thank present you.

**Pattern matching usage** checks whether you lean on Scala’s strengths—sealed ADTs, total matches, explicit handling of
all cases—rather than a maze of ad‑hoc conditionals. It’s less about quantity and more about design: the more your
domain types capture intent, the less conditional friction you carry around.

**Lines of code (LOC)** is less glamorous, but size matters. Large files and classes are hard to hold in your head, hard
to review, and easy to entangle. LOC won’t tell you where bugs are, yet it reliably points to places where smaller seams
and clearer module boundaries will pay dividends.

**Documentation coverage** is a kindness to your future teammates and your library’s users. A well-placed docstring can
eliminate guesswork around a typeclass instance or an implicit conversion.

**Parameter analysis** shines a light on ergonomics: implicit vs. `using`, defaults, and varargs. Context parameters are
powerful; they’re also a form of coupling. When this metric spikes, it’s a cue to ask whether the API could be simpler
or the implicit scope narrower.

**Return type explicitness** is about stability. In public APIs, explicit types communicate intent, protect callers from
inference surprises, and make refactoring safer. Inside a small private helper, inference is fine; at a library
boundary,
clarity wins.

**Inline and implicit usage** captures the “magic level” of a codebase. Inline can be a performance scalpel, and
implicits (or `given`/`using` in Scala 3) can produce beautiful, declarative code. But magic demands documentation and
tests. When these counts grow, it’s a reminder to slow down and explain the trick.

## Summary

| Area                      | Metric                                                 | What it Signals                             | Typical Action                                              |
|---------------------------|--------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------|
| Control flow              | **Cyclomatic Complexity (McCabe)**                     | Branching risk and testing surface          | Extract helpers; add tests around high-complexity regions   |
| Readability               | **Nesting Depth**                                      | Cognitive load from deep control structures | Flatten with guard clauses; refactor nested matches         |
| Compact complexity        | **Expression Branch Density**                          | “Decision per line” intensity               | Expand clever one-liners into named steps                   |
| Idiomatic structure       | **Pattern Matching Usage**                             | Exhaustiveness and ADT hygiene              | Prefer sealed ADTs and total matches                        |
| Size                      | **Lines of Code (LOC) Statistics**                     | Scope and refactor surface area             | Split large files; isolate responsibilities                 |
| API documentation         | **Documentation Coverage**                             | Onboarding friction and misuse risk         | Document public APIs, typeclass instances, conversions      |
| API ergonomics            | **Parameter Analysis** (implicit/using/default/vararg) | Hidden coupling and call-site clarity       | Review `using` contexts; avoid surprising defaults          |
| API stability             | **Return Type Explicitness**                           | Binary/source compatibility and clarity     | Require explicit return types for public members            |
| Compile-time “cleverness” | **Inline & Implicit Usage**                            | Metaprogramming and readability trade-offs  | Gate with tests and docs; prefer `given`/`using` in Scala 3 |

## How to read the numbers without getting lost

The easiest mistake is treating a single threshold as a universal truth. Resist the urge. Parsing combinators will
naturally be more complex than a simple adapter around an HTTP client. Instead:

- Look at **distributions**, not just averages: medians show the typical experience; the 90th percentile reveals the
  pain.
- Track **trends**: a healthy codebase drifts toward shallower nesting, fewer extreme outliers, and better documentation
  over time.
- Compare **like with like**: generated code, migration shims, and test fixtures can distort the picture; excluding them
  yields a more honest baseline.
- Use **guardrails before gates**: start with warnings and gentle nudges in PRs, then tighten once the baseline is
  stable.

A practical rule of thumb for complexity: a function under 10 is usually easy to reason about; 11–20 deserves a closer
look; beyond that, either refactor or surround with strong tests. Treat this as guidance, not law.

## Where to start (without a grand rewrite)

Begin with the hotspots that cause you the most friction today. You can find them by cross‑referencing complexity with
recent churn and file size. In almost every team, a small set of files absorbs a large share of maintenance pain. Pick
one, aim for a small improvement—a split, a few tidy extractions, an added docstring—and ship it. Repeat next week.
You’ll see the dashboard respond.

While you’re at it, enforce two low-drama habits that pay off quickly:

- **Explicit return types** on public members. Private helpers can enjoy inference; public APIs benefit from clarity.
- **Docstrings** on public types and functions, especially around typeclass instances and conversions. They’re your
  first line of communication.

Neither habit slows teams down; both reduce “surprising behavior” bugs and make reviews kinder.

## Folding metrics into everyday work

Metrics are most useful when they’re ambient—visible when you need them, quiet when you don’t. A light‑touch workflow
works well:

- **In pull requests**, add friendly hints. “Nesting depth is 7—could we early‑return on error paths?” You’re not
  policing style; you’re pointing out friction.
- **In CI artifacts or your docs site**, show a tiny dashboard: top hotspots, doc coverage by module, and a trend line
  or two. Keep it boring on purpose.
- **Weekly**, take five minutes to choose one improvement from the hotspot list. Tiny, consistent changes beat heroic
  refactors every time.
- **Before releases**, glance at new or changed hotspots and confirm that public APIs gained docs and explicit return
  types.

The effect is cumulative: fewer sharp edges, fewer surprises, and a codebase that feels lighter to work in.

## Common pitfalls (and how to dodge them)

It’s possible to “game” metrics without improving the code—splitting a monster function into five confusing ones, for
instance. The antidote is qualitative review: ask whether the new shape is _easier to read and test_. Another trap is
applying the same thresholds everywhere. Domain‑heavy parsing and tiny I/O wrappers shouldn’t be judged by a single
ruler. Establish per‑module baselines and you’ll avoid a lot of unproductive debate.

Finally, don’t tighten the screws too early. Blocking builds for modest outliers before you have a baseline can stall
delivery and sour people on the whole idea. Start soft; get the trend moving; then decide where to hold the line.

## What comes next

Each metric has a dedicated page with precise definitions with small Scala code examples, and suggestions. If you’re
skimming, start with cyclomatic complexity, nesting depth, and documentation coverage. If you publish a library, add
return‑type explicitness to that list. From there, follow your pain: when an area confuses reviewers or surprises users,
there’s usually a metric nearby ready to guide you.
<br><br>

---
*Metrics won’t write clean code for you—but they focus attention where it matters, reduce risk, and make your Scala
codebase easier to evolve.*
