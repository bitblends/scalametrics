/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.metrics.model

/**
  * The `Symbol` trait represents a compilation or code-level symbol, providing a wide range of associated metadata and
  * analytical metrics that describe its structural, behavioral, and documentation-level properties.
  *
  * This trait is designed for use cases such as source code analysis, metrics generation, and code quality assessment,
  * offering insights into multiple dimensions such as access modifiers, nesting depths, cyclomatic complexity, pattern
  * matching usages, and branching densities.
  */
trait Symbol {

  /**
    * A unique identifier representing the file associated with this symbol. It links the symbol to the file it
    * originates from or is defined in, facilitating mapping between symbols and source files for analysis.
    */
  val fileId: String

  /**
    * The name of the symbol.
    *
    * Represents the identifier or label associated with this symbol in the source code.
    */
  val name: String

  /**
    * The signature of the symbol.
    *
    * Represents the textual signature of a symbol in the source code, capturing its definition details such as type
    * information, parameters, and other relevant metadata. This can help analyze the structure and definition of the
    * symbol in the context of the source code.
    */
  val signature: String

  /**
    * The access modifier of the symbol in the source code.
    *
    * Specifies the accessibility of the symbol, such as `public`, `protected`, or `private`. This information is used
    * to determine the encapsulation level and visibility of the symbol.
    */
  val accessModifier: String

  /**
    * Represents the total number of lines of code associated with a symbol in the source code.
    */
  val linesOfCode: Int

  /**
    * Indicates whether the symbol has associated Scaladoc documentation.
    */
  val hasScaladoc: Boolean

  /**
    * Indicates whether the symbol is deprecated. Typically, a symbol marked as deprecated will trigger compiler
    * warnings when used.
    */
  val isDeprecated: Boolean

  // Cyclomatic complexity
  /**
    * Represents the cyclomatic complexity of a specific symbol in the codebase. This value indicates the number of
    * independent paths through the symbol's code, reflecting its complexity and potential testing requirements.
    */
  val cComplexity: Int

  // Nesting dept
  /**
    * Represents the nesting depth of code constructs within a symbol. This metric quantifies the depth of nested
    * control structures or blocks in the symbol's code, which can indicate complexity and maintainability concerns.
    */
  val nestingDepth: Int

  // inline
  /**
    * Indicates whether the symbol has the `inline` modifier, which designates that the corresponding member or function
    * should be inlined during compilation for performance or other optimization reasons.
    */
  val hasInlineModifier: Boolean

  // Pattern matching metrics
  /**
    * Represents the number of pattern match expressions present within the code. This value provides a metric for how
    * often pattern matching is utilized.
    */
  val pmMatches: Int

  /**
    * Represents the number of case clauses present within pattern matching constructs in the associated symbol.
    */
  val pmCases: Int

  /**
    * Represents the number of pattern match guards in a symbol.
    *
    * Pattern match guards are boolean expressions used in `case` clauses to further filter matches beyond the standard
    * pattern matching mechanism.
    */
  val pmGuards: Int

  /**
    * Stores the number of wildcard patterns used in pattern match expressions within the symbol.
    */
  val pmWildcards: Int

  /**
    * Represents the maximum level of pattern matching nesting depth within a symbol. This metric provides insights into
    * the complexity of pattern matching constructs present in the symbol.
    */
  val pmMaxNesting: Int

  /**
    * Represents the maximum nesting depth of pattern matches within the associated code structure. This metric provides
    * insight into the complexity of nested pattern matching logic.
    */
  val pmNestedMatches: Int

  /**
    * The average number of cases per match in pattern-matching expressions. This metric provides insight into the
    * complexity and depth of pattern-matching constructs, where a higher value might indicate a greater average
    * branching factor.
    */
  val pmAvgCasesPerMatch: Double

  // Branch density metric
  /**
    * Represents the number of branches in the control flow of a given symbol. Measures branching complexity within the
    * code, including `if`, `case`, and loop constructs.
    */
  val bdBranches: Int

  /**
    * Represents the count of `if` statements (conditional branches) within the associated symbol. This metric is used
    * to analyze the complexity of the symbol in terms of its conditional logic.
    */
  val bdIfCount: Int

  /**
    * Represents the number of case branches found within the associated symbol. Typically used to assess the complexity
    * of pattern matching expressions in the analyzed scope.
    */
  val bdCaseCount: Int

  /**
    * Represents the count of loop constructs (e.g., for, while) in the analyzed symbol.
    */
  val bdLoopCount: Int

  /**
    * Represents the count of `catch` cases in the analyzed codebase. This metric provides insight into error-handling
    * structures by counting the number of distinct catch blocks in the code.
    */
  val bdCatchCaseCount: Int

  /**
    * Represents the total count of boolean operations in the symbol's associated code block, such as logical AND, OR,
    * and NOT operations.
    */
  val bdBoolOpsCount: Int

  /**
    * The ratio of boolean operations to total code elements in a file, multiplied by 100. This metric represents the
    * concentration of boolean logic relative to the overall complexity of the file.
    */
  val bdDensityPer100: Double

  /**
    * Represents the boolean operation density per 100 lines of code. This metric calculates the frequency of boolean
    * operations in a file, normalized to a per-100-lines basis, providing an indication of logic complexity.
    */
  val bdBoolOpsPer100: Double
}
