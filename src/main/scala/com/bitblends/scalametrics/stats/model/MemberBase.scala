/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model
import com.bitblends.scalametrics.metrics.model.{
  BranchDensityMetrics,
  InlineAndImplicitMetrics,
  Metadata,
  PatternMatchingMetrics
}

/**
  * Represents the base properties and metrics of a member within a codebase.
  *
  * This trait defines several attributes that provide detailed information about a code member, including its name,
  * signature, access modifier, metrics related to cyclomatic complexity, nesting depth, pattern matching, and branch
  * density, as well as metadata about deprecated state and Scaladoc presence.
  *
  * Metrics and properties:
  *   - General descriptors such as identifier, name, and signature.
  *   - Indicators of Scaladoc presence and deprecation status.
  *   - Cyclomatic complexity and nesting depth of the member.
  *   - Pattern matching details, including matches, cases, guards, wildcards, maximum nesting, nested matches, and
  *     average cases per match.
  *   - Branch density metrics, including branch counts and related boolean operations.
  */
trait MemberBase {

  /**
    * Represents the metadata associated with a symbol or declaration within this instance.
    *
    * This property contains an instance of the `Metadata` case class, providing detailed information about the symbol's
    * structural and behavioral characteristics, as well as accessibility, nesting, and other related properties.
    */
  val metadata: Metadata

  /**
    * Indicates whether the member has Scaladoc associated with it.
    */
  val hasScaladoc: Boolean

  /**
    * Represents the cyclomatic complexity of a member. Cyclomatic complexity is a software metric used to indicate the
    * complexity of a program. It is calculated based on the number of linearly independent paths through the program's
    * source code.
    */
  val cComplexity: Int

  /**
    * Represents the maximum level of nesting within a member, such as nested blocks, loops, or conditional structures.
    */
  val nestingDepth: Int

  /**
    * Encapsulates metrics related to the usage of inline and implicit constructs within a member.
    *
    * The `InlineAndImplicitStats` field provides detailed statistical data on aspects such as inline modifiers,
    * inline-specific parameters, implicit conversions, abstractness of members, return type information, and usage of
    * `given` constructs specific to Scala 3. It offers insights into the characteristics and behavior of the member in
    * relation to these constructs within the codebase.
    */
  val inlineAndImplicitStats: InlineAndImplicitMetrics

  /**
    * Holds metrics related to the usage and complexity of pattern matching constructs within the analyzed member.
    *
    * The `patternMatchingMetrics` field provides detailed insights such as the total number of match expressions,
    * cases, guards, wildcards, and additional properties like nesting depth and case distribution.
    */
  val patternMatchingMetrics: PatternMatchingMetrics

  /**
    * Represents the branch density metrics associated with a specific code member. The `BranchDensityMetrics` instance
    * provides detailed insights into various branching constructs, their frequency, and density, which helps analyze
    * the complexity and branching logic of the code.
    */
  val branchDensityMetrics: BranchDensityMetrics

}
