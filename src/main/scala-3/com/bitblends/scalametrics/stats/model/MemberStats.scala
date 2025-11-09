/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents statistical and property-based metadata for a specific member definition (e.g., classes, objects, traits,
  * values, methods) in Scala code. This case class is designed to encapsulate a variety of metrics and properties of a
  * member, enabling deeper analysis of code structure, style, and complexity.
  *
  * @constructor
  *   Create a new instance of MemberStats with the given metadata and calculated metrics.
  *
  * @param fileId
  *   Identifier for the source file containing the member.
  * @param name
  *   The name of the member.
  * @param memberType
  *   The type of member (e.g., "val", "var", "type", "class", "object", "trait").
  * @param signature
  *   The textual representation of the member's signature.
  * @param accessModifier
  *   The member's access level (e.g., "public", "private", "protected").
  * @param linesOfCode
  *   The total number of lines spanned by this member, including its body.
  * @param hasScaladoc
  *   Whether the member is documented with Scaladoc.
  * @param isDeprecated
  *   Whether the member is marked as deprecated.
  * @param cComplexity
  *   Cyclomatic complexity of the member, reflecting the number of paths through the code.
  * @param nestingDepth
  *   The depth of nesting of the member (e.g., nested if-else or loops).
  * @param hasInlineModifier
  *   Indicates if the member is defined with the `inline` modifier.
  * @param isGivenInstance
  *   Indicates if the member is a Scala 3 `given` instance.
  * @param isGivenConversion
  *   Indicates if the member is a Scala 3 `given` conversion.
  * @param isImplicit
  *   Indicates if the member is defined with the `implicit` modifier.
  * @param isAbstract
  *   Indicates if the member is defined as abstract.
  * @param hasExplicitReturnType
  *   Specifies whether an explicit return type is defined for the member.
  * @param inferredReturnType
  *   If the return type of the member is inferred, this field may contain its inferred type.
  * @param pmMatches
  *   The total number of pattern matches within the member.
  * @param pmCases
  *   The total number of `case` statements in pattern matches within the member.
  * @param pmGuards
  *   The total number of guards (`if` conditions in `case` statements) in pattern matches.
  * @param pmWildcards
  *   The total number of wildcard patterns (`_`) used in pattern matching.
  * @param pmMaxNesting
  *   The maximum depth of nested pattern matches.
  * @param pmNestedMatches
  *   The total count of nested pattern matches within the member.
  * @param pmAvgCasesPerMatch
  *   The average number of `case` statements per pattern match.
  * @param bdBranches
  *   The total number of branches in the member (e.g., `if`, `case`, loops).
  * @param bdIfCount
  *   The count of `if` constructs in the member.
  * @param bdCaseCount
  *   The count of `case` branches in the member.
  * @param bdLoopCount
  *   The count of loop constructs (`for`, `while`, `do-while`) in the member.
  * @param bdCatchCaseCount
  *   The count of `catch` blocks in try-catch expressions.
  * @param bdBoolOpsCount
  *   The number of boolean operators (`&&`, `||`, etc.) found in the member.
  * @param bdDensityPer100
  *   The branching density of the member, normalized to branches per 100 lines.
  * @param bdBoolOpsPer100
  *   The count of boolean operations per 100 lines.
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
) extends MemberBase:

  /**
    * Converts the fields and their associated values of a case class into a map representation. Each field name is used
    * as a key, and its value is converted to a string representation. For optional fields, `Some` values are
    * represented as their contained value, while `None` is empty. Collections are joined into comma-separated strings.
    *
    * @return
    *   A map where keys are the field names of the case class and values are their string representations.
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
