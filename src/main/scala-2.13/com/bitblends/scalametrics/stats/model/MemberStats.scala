/*
 * SPDX-FileCopyrightText: 2025 Benjamin Saff and contributors
 * SPDX-License-Identifier: MIT
 */

package com.bitblends.scalametrics.stats.model

/**
  * Represents detailed statistics and metadata for a member (e.g., class, object, method, etc.) within a source file.
  * Provides information such as name, type, access modifiers, code metrics, pattern matching statistics, and branch
  * density metrics.
  *
  * @param fileId
  *   The identifier of the file containing the member.
  * @param name
  *   The name of the member.
  * @param memberType
  *   The type of the member, such as "val", "var", "type", "class", "object", or "trait".
  * @param signature
  *   The fully qualified signature of the member, including its parameters and return type if applicable.
  * @param accessModifier
  *   The access modifier (e.g., `public`, `private`, `protected`) of the member.
  * @param linesOfCode
  *   The number of lines of code comprising the member.
  * @param hasScaladoc
  *   Indicates whether the member has associated Scaladoc comments.
  * @param isDeprecated
  *   Indicates whether the member is marked as deprecated.
  * @param cComplexity
  *   The cyclomatic complexity of the member, reflecting the number of independent paths through its code.
  * @param nestingDepth
  *   The nesting depth of the member, representing the extent of nested constructs within it.
  * @param hasInlineModifier
  *   Indicates whether the member includes the `inline` modifier.
  * @param isGivenInstance
  *   Indicates whether the member is defined as a `given` instance (Scala 3 feature).
  * @param isGivenConversion
  *   Indicates whether the member is a `given` conversion (Scala 3 feature).
  * @param isImplicit
  *   Indicates whether the member uses the `implicit` modifier.
  * @param isAbstract
  *   Indicates whether the member is abstract.
  * @param hasExplicitReturnType
  *   Indicates whether the member has an explicitly defined return type.
  * @param inferredReturnType
  *   Optionally specifies the return type of the member if it is inferred.
  * @param pmMatches
  *   The total number of pattern matches in the member.
  * @param pmCases
  *   The total number of case clauses used in pattern matching within the member.
  * @param pmGuards
  *   The total number of guards in case clauses for pattern matching within the member.
  * @param pmWildcards
  *   The count of wildcard matches used in pattern matching within the member.
  * @param pmMaxNesting
  *   The maximum nesting depth of pattern matches within the member.
  * @param pmNestedMatches
  *   The number of nested pattern matches within the member.
  * @param pmAvgCasesPerMatch
  *   The average number of cases per pattern match within the member.
  * @param bdBranches
  *   The total number of branching constructs (e.g., if, case) within the member.
  * @param bdIfCount
  *   The count of `if` constructs in the member.
  * @param bdCaseCount
  *   The count of `case` constructs in the member.
  * @param bdLoopCount
  *   The count of loop constructs within the member.
  * @param bdCatchCaseCount
  *   The count of `catch` case blocks within the member.
  * @param bdBoolOpsCount
  *   The count of boolean operations (e.g., `&&`, `||`) within the member.
  * @param bdDensityPer100
  *   The branching density, calculated as the number of branches per 100 lines of code.
  * @param bdBoolOpsPer100
  *   The density of boolean operations, calculated as the number of boolean operations per 100 lines of code.
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
