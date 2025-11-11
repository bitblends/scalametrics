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
    * Represents metadata encapsulated within a symbol, providing structural and property-related information.
    *
    * This property stores an instance of `Metadata`, containing various attributes of the symbol such as its name,
    * accessibility, nesting level, documentation status, and other relevant metrics that can assist in analyzing the
    * overall characteristics of the symbol.
    */
  val metadata: Metadata

  // Cyclomatic complexity
  /**
    * Represents the cyclomatic complexity of a specific symbol in the codebase. This value indicates the number of
    * independent paths through the symbol's code, reflecting its complexity and potential testing requirements.
    */
  val cComplexity: Int

  /**
    * Indicates whether the symbol has Scaladoc documentation.
    *
    * This property serves as a boolean flag to determine if Scaladoc comments are available for the associated symbol
    * in the codebase.
    */
  val hasScaladoc: Boolean

  // Nesting dept
  /**
    * Represents the nesting depth of code constructs within a symbol. This metric quantifies the depth of nested
    * control structures or blocks in the symbol's code, which can indicate complexity and maintainability concerns.
    */
  val nestingDepth: Int

  /**
    * Represents inline and implicit metrics for a symbol, capturing details such as whether the symbol has the inline
    * modifier, the count of inline parameters, whether it serves as an implicit conversion, and its abstractness or
    * explicit return type characteristics.
    *
    * This field provides insights into the inline and implicit characteristics of the symbol, analyzed through various
    * properties encapsulated in the `InlineAndImplicitMetrics` case class.
    */
  val inlineAndImplicitMetrics: InlineAndImplicitMetrics

  /**
    * Represents pattern matching metrics associated with a symbol in the codebase.
    *
    * This field contains information about the usage, complexity, and structure of pattern matching constructs related
    * to the symbol. It provides insights into the number of matches, cases, guards, wildcards, and their distribution
    * across the analyzed code.
    */
  val patternMatchingMetrics: PatternMatchingMetrics

  /**
    * Represents branch density metrics for the symbol. Provides insights into branching structures and boolean
    * operations within the associated code symbol. It encapsulates details about the number and types of branches,
    * boolean operation complexity, and their density relative to the total code volume.
    */
  val branchDensityMetrics: BranchDensityMetrics

}
