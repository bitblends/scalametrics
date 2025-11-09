/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.model.{PatternMatchingItem, PatternMatchingMetrics}

import scala.meta._

/**
  * Provides utilities for analyzing pattern matching constructs within source code.
  *
  * This object includes methods for processing and analyzing code with a focus on extracting metrics and insights about
  * the usage of pattern matching structures (e.g., `match` expressions, cases, guards, and wildcards). The analysis
  * spans the structure of Scala source code based on abstract syntax trees (ASTs) provided by the relevant Scala
  * parsing library.
  */
object PatternMatching extends Metric[PatternMatchingMetrics] {

  /**
    * Analyzes a given source code string for pattern matching constructs and extracts information about `def` and `val`
    * entities that contain such constructs.
    *
    * This method processes the Scala source code using the specified dialect, identifies the relevant definitions and
    * values, and computes metrics for pattern matching where applicable. The resulting details are encapsulated in a
    * list of `PatternMatchingItem`s.
    *
    * @param code
    *   The source code as a string to be analyzed.
    * @param dialect
    *   The Scala dialect to use for parsing the source code. Defaults to Scala 2.12 dialect.
    * @return
    *   A list of `PatternMatchingItem` instances, each containing information about the identified `def` or `val`
    *   entities along with corresponding pattern matching metrics.
    */
  def analyzeSource(code: String, dialect: Dialect = dialects.Scala212): List[PatternMatchingItem] = {
    val parsed = dialect(code).parse[Source].get
    val buf = scala.collection.mutable.ListBuffer.empty[PatternMatchingItem]

    /**
      * The `Collect` object extends the `TraverserCompat` trait to analyze Scala abstract syntax trees (ASTs) for
      * specific pattern matching constructs within `def` and `val` definitions. It is responsible for traversing the
      * structure of the given Scala source code tree (`Tree`) to extract information and perform computations on
      * matching constructs.
      *
      * Functionality includes:
      *   - Identifying `def` definitions and extracting metrics through the `forDef` method.
      *   - Identifying `val` definitions and extracting metrics through the `forVal` method.
      *   - Aggregating the results into a buffer (`buf`) as instances of `PatternMatchingItem` for further use.
      *
      * The object delegates the traversal of child nodes to the `TraverserCompat` mechanism, ensuring all relevant
      * parts of the tree are inspected.
      *
      * Behavior:
      *   - For `Defn.Def`, it processes the method body through `forDef` and adds a computed `PatternMatchingItem` into
      *     the buffer with information such as the method's name, type ("def"), position, and metrics.
      *   - For `Defn.Val`, it processes the right-hand side of the value definition through `forVal`, determines the
      *     names of defined variables, and adds corresponding `PatternMatchingItem` instances to the buffer.
      *   - For other tree nodes, it continues traversing their children without specific processing.
      */
    object Collect extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {
        case d: Defn.Def =>
          forDef(d).foreach { m => buf += PatternMatchingItem(d.name.value, "def", d.pos, m) }
          traverseChildren(tree)

        case v: Defn.Val =>
          forVal(v).foreach { m =>
            val nm = v.pats.collect { case Pat.Var(n) => n.value }.mkString(",")
            buf += PatternMatchingItem(if (nm.isEmpty) "<val>" else nm, "val", v.pos, m)
          }
          traverseChildren(tree)

        case _ => traverseChildren(tree)
      }
    }

    Collect(parsed)
    buf.toList
  }

  /**
    * Computes pattern matching metrics for a given term by traversing its abstract syntax tree. The method analyzes the
    * term to extract information about matches, cases, guards, wildcards, nesting depth, and other related metrics.
    *
    * @param term
    *   The Scala term to be analyzed.
    * @return
    *   An instance of `PatternMatchingMetrics` containing computed metrics for the term.
    */
  override def compute(term: Term): PatternMatchingMetrics = {
    var mMatches = 0
    var mCases = 0
    var mGuards = 0
    var mWildcards = 0
    var currentDepth = 0
    var maxDepth = 0
    var nestedCount = 0

    /**
      * Executes a provided function while tracking the current depth of nested invocations. Updates the maximum depth
      * and nested invocation count as necessary.
      *
      * @param thunk
      *   A by-name parameter representing the block of code to execute.
      * @return
      *   The result of the executed block.
      */
    def stepMatch[A](thunk: => A): A = {
      currentDepth += 1
      if (currentDepth > maxDepth) maxDepth = currentDepth
      if (currentDepth > 1) nestedCount += 1
      try thunk
      finally currentDepth -= 1
    }

    /**
      * The `Walk` object extends the `TraverserCompat` trait and provides a specialized tree traversal strategy.
      *
      * This object is designed to analyze and process Scala's abstract syntax trees (ASTs) with a focus on pattern
      * matching constructs, such as `Term.Match` and its associated case clauses. Metrics related to matches, cases,
      * guards, wildcards, and nesting depth are updated during the traversal for further analysis and computations.
      *
      * Key Functionality:
      *   - Detect and process `Term.Match` constructs safely using a versioned matcher for future compatibility.
      *   - Traverse the scrutinee expression of a match without increasing the nesting depth.
      *   - Traverse pattern clauses, guards, and case bodies while updating metrics for matches, cases, guards, and
      *     wildcards.
      *   - Maintain nesting depth during traversal of match regions and their case bodies.
      *   - For all other constructs not related to `Term.Match`, it delegates to the `TraverserCompat`'s
      *     `traverseChildren` method.
      */
    object Walk extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {

        // Versioned matcher (future-safe)
        case Term.Match.Initial(expr, cases) =>
          mMatches += 1
          // do NOT increase depth for the scrutinee expr
          apply(expr)
          // entering this match's *arm bodies* counts as a nesting level
          val thisMatchCaseCount = cases.size
          mCases += thisMatchCaseCount
          // step depth for the whole "match region" while traversing case bodies
          stepMatch {
            cases.foreach { c =>
              // pattern & guard first (no depth change)
              apply(c.pat)
              c.cond.foreach { g =>
                mGuards += 1
                apply(g)
              }
              // count wildcard/default case
              c.pat match {
                case Pat.Wildcard() => mWildcards += 1
                case _              => ()
              }
              // now the case body inside the match region (can contain nested matches)
              apply(c.body)
            }
          }
        // done with this match; continue

        // keep walking everything else
        case _ =>
          traverseChildren(tree)
      }
    }

    Walk(term)

    PatternMatchingMetrics(
      matches = mMatches,
      cases = mCases,
      guards = mGuards,
      wildcards = mWildcards,
      maxMatchNesting = maxDepth,
      nestedMatches = nestedCount,
      perMatchCases = Nil
    )
  }

}
