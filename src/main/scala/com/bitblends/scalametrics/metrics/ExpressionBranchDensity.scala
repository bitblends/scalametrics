/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics

import com.bitblends.scalametrics.analyzer.model.{BranchDensityItem, BranchDensityMetrics}

import scala.meta._

/**
  * Provides utilities to compute branch density metrics for Scala code expressions, specifically analyzing structures
  * like branches, loops, and boolean operations to derive metrics that summarize the logical complexity of code.
  */
object ExpressionBranchDensity extends Metric[BranchDensityMetrics] {

  /**
    * Computes branch density metrics for a given Scala meta `Term`.
    *
    * The method traverses the syntax tree of the provided `Term` to count various branching constructs such as `if`
    * statements, `case` branches in match expressions, loops (including `while`, `do`, `for`, and `for-yield`), `catch`
    * clauses in `try` expressions, and boolean operations (`&&` and `||`). Additionally, it computes the total number
    * of physical lines of code (LOC).
    *
    * @param term
    *   The Scala meta `Term` whose branch density metrics are to be computed. This term is analyzed to extract detailed
    *   information about branching constructs.
    * @return
    *   A `BranchDensityMetrics` instance containing detailed metrics such as LOC, total branches, counts of various
    *   branching constructs (`if`, `case`, `loop`, `catch`, and boolean operations).
    */
  override def compute(term: Term): BranchDensityMetrics = {
    var ifs = 0
    var cases = 0
    var loops = 0
    var catchCases = 0
    var boolOps = 0

    /**
      * Represents a tree traversal logic for computing branching density metrics.
      *
      * The `Walk` object extends `TraverserCompat` and overrides the `apply` method to traverse a syntax tree (`Tree`)
      * and collect metrics related to branching constructs. The traversal logic identifies and computes counts for
      * constructs such as conditionals, match cases, loops, try-catch blocks, and short-circuit boolean operations,
      * incrementing the corresponding counters during the traversal process.
      *
      * The key branching constructs include:
      *   - If and Else-If statements: Counts each occurrence of conditional constructs.
      *   - Match expressions: Counts the number of case branches.
      *   - Loops: Includes counts for `while`, `do-while`, `for`, and `for-yield` loops.
      *   - Try expressions: Captures the number of catch blocks.
      *   - Short-circuit Boolean operations: Tracks occurrences of `&&` and `||` operations.
      *
      * The traversal proceeds recursively for child nodes using the `traverseChildren` method provided by the
      * `TraverserCompat` trait.
      */
    object Walk extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {

        // If / else-if
        case Term.If.Initial(cond, thenp, elsep) =>
          ifs += 1
          traverseChildren(tree) // continue inside

        // Match: count cases (branch points)
        case Term.Match.Initial(expr, cs) =>
          cases += cs.size
          traverseChildren(tree)

        // Loops
        case _: Term.While =>
          loops += 1; traverseChildren(tree)

        case _: Term.Do =>
          loops += 1; traverseChildren(tree)

        case _: Term.For =>
          loops += 1; traverseChildren(tree)

        case _: Term.ForYield =>
          loops += 1; traverseChildren(tree)

        // Try: each catch case is a branch
        case t: Term.Try =>
          catchCases += t.catchp.size
          traverseChildren(tree)

        // Short-circuit booleans
        case Term.ApplyInfix(_, op, _, _) if op.value == "&&" || op.value == "||" =>
          boolOps += 1
          traverseChildren(tree)

        case _ =>
          traverseChildren(tree)
      }
    }

    Walk(term)

    val totalBranches =
      ifs + cases + loops + catchCases + boolOps

    BranchDensityMetrics(
      loc = locOf(term),
      branches = totalBranches,
      ifCount = ifs,
      caseCount = cases,
      loopCount = loops,
      catchCaseCount = catchCases,
      boolOpsCount = boolOps
    )
  }

  /**
    * Computes the physical lines of code (LOC) for a given Scala meta `Term`.
    *
    * The method calculates the difference between the starting and ending line numbers of the term's position and
    * ensures the result is non-negative.
    *
    * @param term
    *   The Scala meta `Term` whose physical lines of code are to be computed. The `Term` contains metadata that
    *   includes the code's position in terms of start and end line numbers.
    * @return
    *   An `Int` representing the number of physical lines of code for the given term. If the term's position is invalid
    *   or lines cannot be determined, the result is 0.
    */
  private def locOf(term: Term): Int = {
    val p = term.pos
    // physical LOC from body start to end (inclusive)
    (p.endLine - p.startLine + 1) max 0
  }

  /**
    * Analyzes the provided Scala source code to compute branch density metrics for method and value definitions.
    *
    * This method parses the given Scala source code, traverses the abstract syntax tree (AST), and collects branch
    * density metrics for all `Defn.Def` (method definitions) and `Defn.Val` (value definitions) encountered in the
    * code. The result is a list of `BranchDensityItem` instances, each representing a code entity with its associated
    * branch density metrics.
    *
    * @param code
    *   The Scala source code to analyze. This code is parsed using the specified dialect into an abstract syntax tree
    *   (AST).
    * @param dialect
    *   The Scala dialect to use for parsing the source code. The default value is `dialects.Scala212`.
    * @return
    *   A list of `BranchDensityItem` instances, where each item contains the name, kind, position, and branch density
    *   metrics of a code entity (method or value definition) in the source code.
    */
  def analyzeSource(code: String, dialect: Dialect = dialects.Scala212): List[BranchDensityItem] = {
    val parsed = dialect(code).parse[Source].get
    val buf = scala.collection.mutable.ListBuffer.empty[BranchDensityItem]

    /**
      * Object extending `TraverserCompat` for collecting branch density metrics for certain Scala meta AST nodes.
      *
      * This object processes method definitions (`Defn.Def`) and value definitions (`Defn.Val`) to compute branch
      * density metrics. The collected metrics are stored in a buffer as instances of `BranchDensityItem`. During
      * traversal, it processes the body or right-hand side of the definitions if they are terms, extracts relevant
      * information such as name, kind, position, and metrics, and performs recursive traversal of child nodes.
      *
      * The following cases are matched and processed during traversal:
      *   - `Defn.Def`: Extracts the body of the method, computes branch density metrics using `forDef`, and collects
      *     the result.
      *   - `Defn.Val`: Extracts the right-hand side of the value definition, computes branch density metrics using
      *     `forVal`, builds the name from the variable patterns (if available), and collects the result.
      *   - Other cases: Traverses child nodes without additional processing.
      *
      * This object is used in the context of computing metrics for code entities in Scala source code. It assumes
      * compatibility with Scala meta tree structures.
      */
    object Collect extends TraverserCompat {
      override def apply(tree: Tree): Unit = tree match {
        case d: Defn.Def =>
          forDef(d).foreach(m => buf += BranchDensityItem(d.name.value, "def", d.pos, m))
          traverseChildren(tree)
        case v: Defn.Val =>
          forVal(v).foreach { m =>
            val nm = v.pats.collect { case Pat.Var(n) => n.value }.mkString(",")
            buf += BranchDensityItem(if (nm.isEmpty) "<val>" else nm, "val", v.pos, m)
          }
          traverseChildren(tree)
        case _ => traverseChildren(tree)
      }
    }

    Collect(parsed)
    buf.toList
  }

}
