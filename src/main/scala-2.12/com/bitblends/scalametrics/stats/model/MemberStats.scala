/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents detailed statistics for a member in Scala code, encapsulating various metrics and properties to analyze
  * code quality, complexity, and usage patterns.
  *
  * @param fileId
  *   The unique identifier of the file containing the member.
  * @param name
  *   The name of the member (e.g., class, method, variable).
  * @param memberType
  *   The type of the member (e.g., "val", "var", "class", "object", "trait").
  * @param signature
  *   The member's signature.
  * @param accessModifier
  *   The access level (e.g., "public", "protected", "private").
  * @param linesOfCode
  *   The number of lines of code for the member.
  * @param hasScaladoc
  *   Indicates whether the member includes Scaladoc.
  * @param isDeprecated
  *   Indicates if the member is marked as deprecated.
  * @param cComplexity
  *   The cyclomatic complexity of the member.
  * @param nestingDepth
  *   The maximum nesting depth within the member.
  * @param hasInlineModifier
  *   True if the member contains the `inline` modifier (Scala 3).
  * @param isGivenInstance
  *   Indicates whether this is a Scala 3 `given` instance.
  * @param isGivenConversion
  *   Indicates whether this is a Scala 3 `given` for implicit conversion.
  * @param isImplicit
  *   True if the member is marked as implicit (Scala 2 or 3).
  * @param isAbstract
  *   Indicates if the member is abstract.
  * @param hasExplicitReturnType
  *   True if the member has an explicitly defined return type.
  * @param inferredReturnType
  *   The inferred return type of the member, if applicable.
  * @param pmMatches
  *   Number of pattern match expressions related to the member.
  * @param pmCases
  *   Total number of cases across all pattern matches.
  * @param pmGuards
  *   Number of guards in pattern match cases.
  * @param pmWildcards
  *   Number of wildcard patterns used.
  * @param pmMaxNesting
  *   Maximum nesting depth of pattern match expressions.
  * @param pmNestedMatches
  *   Total nested pattern matches.
  * @param pmAvgCasesPerMatch
  *   The average number of cases per match expression.
  * @param bdBranches
  *   Total number of branches for branch density analysis.
  * @param bdIfCount
  *   Number of `if` branches.
  * @param bdCaseCount
  *   Number of `case` branches.
  * @param bdLoopCount
  *   Number of loops encountered in the member.
  * @param bdCatchCaseCount
  *   Number of `catch` cases.
  * @param bdBoolOpsCount
  *   The number of boolean operations used.
  * @param bdDensityPer100
  *   Branch density per 100 lines of code.
  * @param bdBoolOpsPer100
  *   Boolean operation density per 100 lines of code.
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
    * Converts the current instance into a Map where field names are mapped to their corresponding values as strings.
    * Special handling is applied to optional and sequence types to ensure proper string representation.
    *
    * @return
    *   a map where keys represent field names and values correspond to their string representations.
    */
  override def toMap: Map[String, String] = {
    val fieldNames = List(
      "fileId",
      "name",
      "memberType",
      "signature",
      "accessModifier",
      "linesOfCode",
      "hasScaladoc",
      "isDeprecated",
      "cComplexity",
      "nestingDepth",
      "hasInlineModifier",
      "isGivenInstance",
      "isGivenConversion",
      "isImplicit",
      "isAbstract",
      "hasExplicitReturnType",
      "inferredReturnType",
      "pmMatches",
      "pmCases",
      "pmGuards",
      "pmWildcards",
      "pmMaxNesting",
      "pmNestedMatches",
      "pmAvgCasesPerMatch",
      "bdBranches",
      "bdIfCount",
      "bdCaseCount",
      "bdLoopCount",
      "bdCatchCaseCount",
      "bdBoolOpsCount",
      "bdDensityPer100",
      "bdBoolOpsPer100"
    )
    fieldNames
      .zip(this.productIterator.toList)
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
