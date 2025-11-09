/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.model.NestingDepthItem

import scala.meta._

/**
  * Provides functionality for analyzing the nesting depth of Scala source code. The object calculates the levels of
  * nested constructs like method definitions, blocks, conditionals, matches, loops, and other structural elements of a
  * Scala program.
  *
  * We add +1 when we ENTER bodies of: blocks, if-then/else, case bodies, loop bodies (for/while/do), try/catch/finally
  * bodies, lambdas, partial functions.
  */
object NestingDepth extends Metric[Int] {

  /**
    * Analyzes the source code to compute the nesting depth of various constructs, such as functions and values, and
    * returns a list of nesting depth items.
    *
    * @param code
    *   The source code to be analyzed as a String.
    * @param dialect
    *   The Scala dialect used to parse the source code. Defaults to `dialects.Scala212`.
    * @return
    *   A list of `NestingDepthItem` objects, each representing the nesting depth of a specific construct (e.g., `def`
    *   or `val`) along with its metadata such as name, type, position, and depth.
    */
  def analyzeSource(code: String, dialect: Dialect = dialects.Scala212): List[NestingDepthItem] = {
    val parsed = dialect(code).parse[Source].get
    val buf = scala.collection.mutable.ListBuffer.empty[NestingDepthItem]

    /**
      * The `Collect` object extends `TraverserCompat` and provides functionality to traverse a syntax tree and collect
      * information about the nesting depth of certain language constructs such as method definitions and value
      * definitions.
      *
      * During traversal, it analyzes `def` and `val` declarations, computes their nesting depth, and adds them to a
      * collection in the form of `NestingDepthItem`. The object ensures that all child nodes of the syntax tree are
      * traversed recursively.
      */
    object Collect extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {
        case d: Defn.Def =>
          forDef(d).foreach { depth =>
            buf += NestingDepthItem(d.name.value, "def", d.pos, depth)
          }
          traverseChildren(tree)

        case v: Defn.Val =>
          forVal(v).foreach { depth =>
            val nm = v.pats.collect { case Pat.Var(n) => n.value }.mkString(",")
            buf += NestingDepthItem(if (nm.isEmpty) "<val>" else nm, "val", v.pos, depth)
          }
          traverseChildren(tree)

        case _ => traverseChildren(tree)
      }
    }

    Collect(parsed)
    buf.toList
  }

  /**
    * Computes the deepest level of nesting present in the specified term. This function traverses the syntax tree
    * represented by the provided term to determine the maximum nesting depth of various language constructs such as
    * blocks, conditionals, loops, match expressions, and lambdas.
    *
    * @param term
    *   The root term of the syntax tree to analyze for nesting depth. Represented as an instance of `Term`.
    * @return
    *   An integer representing the maximum observed nesting depth within the syntax tree.
    */
  override def compute(term: Term): Int = {
    var current = 0
    var maxSeen = 0

    /**
      * Executes a block of code (thunk) with optional tracking of nesting depth. If `enter` is true, the current
      * nesting depth is incremented before executing the block and decremented afterward. Additionally, the maximum
      * observed nesting depth is updated if the current depth exceeds the previous maximum.
      *
      * @param enter
      *   A boolean flag indicating whether to adjust the nesting depth counter. If true, the nesting depth is
      *   incremented before executing the block and decremented afterward.
      * @param thunk
      *   The block of code to execute, represented as a call-by-name parameter.
      * @return
      *   The result of the executed block of code.
      */
    def step[A](enter: Boolean)(thunk: => A): A = {
      if (enter) {
        current += 1;
        if (current > maxSeen) maxSeen = current
      }
      try thunk
      finally if (enter) current -= 1
    }

    /**
      * An object that extends `TraverserCompat` to implement logic for traversing and analyzing the nesting depth of
      * Scala syntax trees. The object provides a mechanism for stepping through various Scala constructs such as
      * control structures, match expressions, loops, lambdas, and try/catch blocks, while maintaining a notion of
      * nesting depth.
      *
      * The primary responsibility of this object is to traverse a given tree structure, applying custom rules for
      * interpreting the nesting depth of specific constructs.
      */
    object Walk extends TraverserCompat {
      // Helper to apply a term that is the direct body of a control structure
      // Single-statement blocks are syntactic sugar - don't count them
      // Multi-statement blocks are semantic blocks - DO count them
      def applyControlBody(t: Term): Unit = t match {
        case b: Term.Block if b.stats.size == 1 =>
          // Single-statement block - syntactic sugar, don't count it
          // Just process the single statement directly
          b.stats.head match {
            case stat: Term => apply(stat)
            case stat       => traverseChildren(stat)
          }
        case b: Term.Block =>
          // Multi-statement block - this is a real semantic block, count it
          step(true) {
            traverseChildren(b)
          }
        case other => apply(other)
      }

      override def apply(tree: Tree): Unit = tree match {

        // Blocks
        case b: Term.Block =>
          step(true) {
            traverseChildren(b)
          }

        // If (versioned matcher)
        case Term.If.Initial(cond, thenp, elsep) =>
          apply(cond)
          step(true) {
            applyControlBody(thenp)
          }
          elsep match {
            case Lit.Unit() => () // no else in source
            // Special case: if-else-if chain (else contains only another if)
            // Don't add extra depth for the else branch, just process the nested if
            case elseIf @ Term.If.Initial(_, _, _) =>
              apply(elseIf)
            case other =>
              step(true) {
                applyControlBody(other)
              }
          }

        // Match (versioned matcher) — step into each case body
        case Term.Match.Initial(expr, cases) =>
          apply(expr)
          cases.foreach { c =>
            apply(c.pat);
            c.cond.foreach(apply)
            step(true) {
              applyControlBody(c.body)
            }
          }

        // While / Do - loops never count their direct body block
        case w: Term.While =>
          apply(w.expr);
          step(true) {
            w.body match {
              case b: Term.Block => traverseChildren(b) // Never count loop body blocks
              case other         => apply(other)
            }
          }

        case d: Term.Do =>
          step(true) {
            d.body match {
              case b: Term.Block => traverseChildren(b) // Never count loop body blocks
              case other         => apply(other)
            }
          };
          apply(d.expr)

        // For / ForYield
        case f: Term.For =>
          f.enums.foreach(apply);
          step(true) {
            applyControlBody(f.body)
          }

        case fy: Term.ForYield =>
          fy.enums.foreach(apply);
          step(true) {
            applyControlBody(fy.body)
          }

        // Try / catch / finally
        case t: Term.Try =>
          step(true) {
            applyControlBody(t.expr)
          }
          t.catchp.foreach { c =>
            apply(c.pat);
            c.cond.foreach(apply)
            step(true) {
              applyControlBody(c.body)
            }
          }
          t.finallyp.foreach(fin =>
            step(true) {
              applyControlBody(fin)
            }
          )

        // Lambdas / partial functions
        case f: Term.Function =>
          f.params.foreach(apply)
          step(true) {
            applyControlBody(f.body)
          }

        case pf: Term.PartialFunction =>
          pf.cases.foreach { c =>
            apply(c.pat);
            c.cond.foreach(apply)
            step(true) {
              applyControlBody(c.body)
            }
          }

        case _ => traverseChildren(tree)
      }
    }

    Walk(term)
    maxSeen
  }

}
