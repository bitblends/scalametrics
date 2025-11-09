/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.model.CyclomaticItem

import scala.meta._

/**
  * Utility object for computing cyclomatic complexity (CC) of Scala code.
  *
  * Cyclomatic complexity is a software metric used to measure the logical complexity of a program. It is calculated
  * through the determination of the number of independent paths through a piece of code. This object provides methods
  * for analyzing Scala source code and computing CC for functions, values, and terms.
  */
object Cyclomatic extends Metric[Int] {

  /**
    * Calculates the cyclomatic complexity (CC) of a function or value declaration by analyzing the source code.
    *
    * @param name
    *   The name of the function or value whose cyclomatic complexity is to be computed.
    * @param code
    *   The source code to analyze, potentially containing the target declaration.
    * @param dialect
    *   The dialect of the Scala source code (defaults to Scala 2.12).
    * @return
    *   An Option containing the cyclomatic complexity (as an Int) of the specified function or value if it exists in
    *   the analyzed source code, or None if the declaration cannot be found.
    */
  def ccOf(name: String, code: String, dialect: Dialect = dialects.Scala212): Option[Int] =
    analyzeSource(code, dialect).find(_.name == name).map(_.cc)

  /**
    * Analyzes the given source code and collects cyclomatic complexity (CC) metrics for all function (`def`) and value
    * (`val`) declarations in the code.
    *
    * Cyclomatic complexity is a measure of the decision complexity of code, representing the number of linearly
    * independent paths through a program's source.
    *
    * @param code
    *   The source code to be analyzed. It must be a valid Scala source code string.
    * @param dialect
    *   The dialect of Scala to use when parsing the source code. Defaults to `Scala 2.12`.
    * @return
    *   A list of `CyclomaticItem` instances, each representing the cyclomatic complexity and associated metadata (name,
    *   kind, and position) for each analyzed function or value declaration in the source code.
    */
  def analyzeSource(code: String, dialect: Dialect = dialects.Scala212): List[CyclomaticItem] = {
    val parsed = dialect(code).parse[Source].get
    val buf = scala.collection.mutable.ListBuffer.empty[CyclomaticItem]

    /**
      * Companion object responsible for traversing Scala AST (`Tree`) structures to collect cyclomatic complexity (CC)
      * metrics for `def` and `val` declarations.
      *
      * `Collect` extends the functionality of `TraverserCompat` and overrides its `apply` method to analyze specific
      * nodes in the tree structure. The object matches on `Defn.Def` (method/function definitions) and `Defn.Val`
      * (value declarations) to compute and store their cyclomatic complexity measurements.
      *
      * Each detected declaration is processed using the functions `forDef` or `forVal` to calculate the cyclomatic
      * complexity and generate a `CyclomaticItem` representing the result. The items are added to the internal buffer
      * (`buf`) for further analysis or reporting.
      *
      * Supported tree nodes:
      *   - `Defn.Def`: A function/method definition. The cyclomatic complexity is calculated for its body using
      *     `forDef`.
      *   - `Defn.Val`: A value definition. Cyclomatic complexity is computed for the right-hand side using `forVal`.
      *   - Other nodes: Delegated to `traverseChildren` for further traversal.
      */
    object Collect extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {
        case d: Defn.Def =>
          forDef(d).foreach { cc =>
            buf += CyclomaticItem(d.name.value, "def", d.pos, cc)
          }
          traverseChildren(tree)

        case v: Defn.Val =>
          forVal(v).foreach { cc =>
            val nm = v.pats.collect { case Pat.Var(n) => n.value }.mkString(",")
            buf += CyclomaticItem(if (nm.isEmpty) "<val>" else nm, "val", v.pos, cc)
          }
          traverseChildren(tree)

        case other =>
          traverseChildren(other)
      }
    }

    Collect(parsed)
    buf.toList
  }

  /**
    * Computes the cyclomatic complexity (CC) of the given term by traversing its tree structure and counting the
    * control flow constructs such as if statements, loops, match expressions, guards, and try-catch blocks.
    *
    * @param term
    *   the `Term` syntax tree representation for which cyclomatic complexity is to be computed
    * @return
    *   the cyclomatic complexity of the given term as an `Int`
    */
  override def compute(term: Term): Int = {
    var n = 1 // base path

    /**
      * Walk is a specialized object for traversing the Abstract Syntax Tree (AST) of Scala source code to compute
      * metrics related to cyclomatic complexity. It identifies and counts the control flow structures such as
      * conditional expressions, loops, match expressions, try-catch blocks, and logical operations.
      *
      * This object extends the TraverserCompat trait to leverage the traversal mechanisms for parsing Scala syntax
      * trees. During traversal, it computes values related to cyclomatic complexity by incrementing counters for
      * various control flow constructs.
      *
      * The following control flow constructs are specifically handled:
      *   - `if` statements
      *   - `match` expressions
      *   - `while` loops
      *   - `do-while` loops
      *   - `for` and `for-yield` comprehensions, including guards
      *   - `try-catch` blocks
      *   - Short-circuiting boolean operators (`&&` and `||`)
      *   - Case guards within match statements (`case ... if ...`)
      *
      * When a relevant construct is encountered, the counter `n` is incremented based on the complexity of the
      * structure (e.g., the number of match cases or for-guards).
      */
    object Walk extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {
        case _: Term.If =>
          n += 1
          traverseChildren(tree)

        case m: Term.Match =>
          n += m.cases.size
          traverseChildren(tree)

        case _: Term.While =>
          n += 1
          traverseChildren(tree)

        case _: Term.Do =>
          n += 1
          traverseChildren(tree)

        case f: Term.For =>
          n += 1
          n += f.enums.count(_.isInstanceOf[Enumerator.Guard]) // for-guards
          traverseChildren(f)

        case fy: Term.ForYield =>
          n += 1
          n += fy.enums.count(_.isInstanceOf[Enumerator.Guard]) // for-guards
          traverseChildren(fy)

        case t: Term.Try =>
          n += t.catchp.size // # catch cases
          traverseChildren(t)

        // boolean short-circuit ops: &&, ||
        case Term.ApplyInfix(_, op, _, _) if op.value == "&&" || op.value == "||" =>
          n += 1
          traverseChildren(tree)

        // case guards: case ... if ...
        case c: Case =>
          if (c.cond.nonEmpty) n += 1
          traverseChildren(c)

        case other =>
          traverseChildren(other)
      }
    }

    Walk(term)
    n
  }

}
