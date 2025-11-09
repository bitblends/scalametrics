/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

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
    * Represents the unique identifier for the source file associated with this code member.
    *
    * This identifier helps associate a codebase member with its corresponding file, enabling easier tracing,
    * organization, and analysis of source files within the project.
    */
  val fileId: String

  /**
    * The name of the codebase member.
    *
    * Represents the identifier used to label the member, providing clarity and context within the codebase. The `name`
    * typically corresponds to the user-defined name of a class, trait, method, or other similar constructs.
    */
  val name: String

  /**
    * Represents the member's defining code signature as a string.
    */
  val signature: String

  /**
    * Represents the access modifier of a member. Provides information about the visibility level or scope of the
    * member.
    */
  val accessModifier: String

  /**
    * Represents the number of lines of code associated with a particular member. This value provides insight into the
    * size or complexity of the member in terms of its implementation.
    */
  val linesOfCode: Int

  /**
    * Indicates whether the member has Scaladoc associated with it.
    */
  val hasScaladoc: Boolean

  /**
    * Indicates whether the associated member is marked as deprecated.
    */
  val isDeprecated: Boolean

  // Cyclomatic complexity ------------------------------------------
  /**
    * Represents the cyclomatic complexity of a member. Cyclomatic complexity is a software metric used to indicate the
    * complexity of a program. It is calculated based on the number of linearly independent paths through the program's
    * source code.
    */
  val cComplexity: Int

  // Nesting dept ------------------------------------------
  /**
    * Represents the maximum level of nesting within a member, such as nested blocks, loops, or conditional structures.
    */
  val nestingDepth: Int

  // Inline ------------------------------------------
  /**
    * Indicates whether the member has the `inline` modifier.
    */
  val hasInlineModifier: Boolean

  // Pattern matching metrics ------------------------------------------
  /**
    * Represents the number of pattern-matching constructs within the member.
    */
  val pmMatches: Int

  /**
    * Represents the number of pattern match cases in a given entity.
    */
  val pmCases: Int

  /**
    * Represents the number of pattern matching guards associated with the member.
    */
  val pmGuards: Int

  /**
    * Represents the number of pattern matching wildcards identified in the corresponding member.
    */
  val pmWildcards: Int

  /**
    * Represents the maximum nesting depth of pattern matches within the member. Useful for analyzing the complexity of
    * pattern matching logic.
    */
  val pmMaxNesting: Int

  /**
    * Represents the total count of pattern matches within nested constructs in the source code analyzed for this
    * member. The value is an aggregated metric specific to the evaluated context.
    */
  val pmNestedMatches: Int

  /**
    * Represents the average number of cases per match in pattern matching constructs within the member.
    */
  val pmAvgCasesPerMatch: Double

  // Branch density metric ------------------------------------------
  /**
    * Represents the number of branches in the block or decision structure of the associated member. This metric is
    * typically used in complexity analysis to indicate how many branching points exist within the code for the member.
    */
  val bdBranches: Int

  /**
    * Represents the count of `if` branches present within the member.
    */
  val bdIfCount: Int

  /**
    * Represents the total number of case statements analyzed or counted in the given context. Typically used as a
    * metric to track or measure code complexity related to pattern matching structures.
    */
  val bdCaseCount: Int

  /**
    * Represents the count of loop constructs identified in the member analysis.
    */
  val bdLoopCount: Int

  /**
    * Represents the count of catch cases found within a block of code associated with this member. This field is used
    * to analyze the structural complexity in terms of error-handling logic.
    */
  val bdCatchCaseCount: Int

  /**
    * Represents the number of boolean operations found in the analyzed member. This value is used as a metric to
    * evaluate logical complexity within the member's implementation.
    */
  val bdBoolOpsCount: Int

  /**
    * Represents the branch density percentage multiplied by 100. This metric gives an indication of the proportion of
    * branching logic present in the code, relative to the evaluated lines of code.
    */
  val bdDensityPer100: Double

  /**
    * Represents the count of boolean operations per 100 lines of code in the context of the MemberBase.
    */
  val bdBoolOpsPer100: Double
  // ------------------------------------------------------------------------------------

  /**
    * Converts the current instance into a `Map[String, String]` representation, where the keys and values represent the
    * properties of the instance.
    *
    * @return
    *   A map containing string representations of the instance's key-value pairs.
    */
  def toMap: Map[String, String]
}
