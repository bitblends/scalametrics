/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents detailed metrics and properties of a member in a Scala codebase.
  *
  * This case class extends the `MemberBase` trait to model a member's descriptive and metric-based properties, such as
  * its type, access modifier, code structure details, whether it has Scaladoc, and additional metrics related to
  * pattern matching and branch density. Below is an overview of the member's attributes provided by this class:
  *
  *   - General identifiers and descriptors: Includes the member's name, unique file identifier, and its signature.
  *   - Member type and modifiers: Specifies the type of member (e.g., `val`, `var`, `class`, `trait`), access modifiers
  *     (public, private, etc.), whether it is abstract, implicit, or has inline modifiers, among others.
  *   - Scaladoc and deprecation status: Indicates whether the member is documented with Scaladoc and whether it is
  *     marked as deprecated.
  *   - Code complexity and structure: Includes metrics such as cyclomatic complexity and nesting depth, and provides
  *     information about explicit return types and inferred return types.
  *   - Pattern matching metrics: Includes details like the number of matches, cases, guards, wildcards, and nested
  *     matches, as well as the maximum nesting level and average number of cases per match.
  *   - Branch density metrics: Provides counts related to branches, `if` statements, `case` statements, loops, boolean
  *     operations, and includes metrics such as branch density and boolean operations density per 100 lines of code.
  *
  * It provides a structured representation suitable for modeling metrics and metadata at a member level in the context
  * of analyzing and reporting codebase statistics.
  */
case class MemberStats(
    fileId: String,
    name: String,
    memberType: String, // "val", "var", "type", "class", "object", "trait",...
    signature: String,
    accessModifier: String,
    linesOfCode: Int,
    hasScaladoc: Boolean,
    isDeprecated: Boolean,
    // Cyclomatic complexity
    cComplexity: Int,
    // Nesting depth
    nestingDepth: Int,
    // inline
    hasInlineModifier: Boolean,
    // given
    isGivenInstance: Boolean, // Scala 3 'given'
    isGivenConversion: Boolean,
    // implicit
    isImplicit: Boolean,
    // explicitness
    isAbstract: Boolean,
    hasExplicitReturnType: Boolean,
    inferredReturnType: Option[String] = None,
    // Pattern matching metrics
    pmMatches: Int,
    pmCases: Int,
    pmGuards: Int,
    pmWildcards: Int,
    pmMaxNesting: Int,
    pmNestedMatches: Int,
    pmAvgCasesPerMatch: Double,
    // Branch density metrics
    bdBranches: Int,
    bdIfCount: Int,
    bdCaseCount: Int,
    bdLoopCount: Int,
    bdCatchCaseCount: Int,
    bdBoolOpsCount: Int,
    bdDensityPer100: Double,
    bdBoolOpsPer100: Double
) extends MemberBase {

  /**
    * Converts the fields of the current product instance into a `Map[String, String]`, where the keys correspond to the
    * field names and the values are their string representations.
    *
    * The method handles the following value types:
    *   - `Some(v)`: Converts the contained value `v` to its string representation.
    *   - `None`: Maps to an empty string.
    *   - `Seq[_]`: Converts the sequence into a comma-separated string.
    *   - Any other value: Converts directly to its string representation.
    *
    * @return
    *   a map containing the field names as keys and their corresponding string values, properly transformed based on
    *   their types.
    */
  def toMap: Map[String, String] = {
    this.productElementNames
      .zip(this.productIterator)
      .map { case (name, value) =>
        name -> (value match {
          case Some(v)     => v.toString
          case None        => ""
          case seq: Seq[_] => seq.mkString(",")
          case v           => v.toString
        })
      }
      .toMap
  }
}
